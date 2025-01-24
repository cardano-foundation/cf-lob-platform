package org.cardanofoundation.lob.app.blockchain_publisher.service;

import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.ReportEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class WatchDogService {

    private final OrganisationPublicApiIF organisationPublicApiIF;
    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;
    private final BlockchainReaderPublicApiIF blockchainReaderPublicApi;
    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final ReportEntityRepositoryGateway reportEntityRepositoryGateway;

    @Value("${lob.blockchain_publisher.watchdog.rollback.grace.period.minutes:15}")
    @Getter
    @Setter
    private int rollbackGracePeriodMinutes = 15;

    @Value("${lob.blockchain_publisher.watchdog.rollbacks.enabled:true}")
    @Getter
    @Setter
    private boolean rollbackSupportEnabled = true;

    @PostConstruct
    public void init() {
        log.info("TransactionsWatchDogService configuration: rollbackGracePeriodMinutes={}, rollbacksEnabled={}", rollbackGracePeriodMinutes, rollbackSupportEnabled);

        log.info("TransactionsWatchDogService started");
    }

    public void checkTxStatusesForOrganisations(int txStatusInspectionLimitPerOrgPullSize) {
        organisationPublicApiIF.listAll().forEach(org -> {
            log.info("Checking transaction statuses for organisation: {}", org.getName());
            checkTransactionStatusesForOrganisation(org, txStatusInspectionLimitPerOrgPullSize);
        });
    }

    private void checkTransactionStatusesForOrganisation(Organisation org, int txStatusInspectionLimitPerOrgPullSize) {
        ChainTip chainTip = getChainTip();
        if (!chainTip.isSynced()) {
            log.info("Chain is not synced, skipping transaction status check for organisation: {}", org.getName());
            return;
        }

        transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(org.getId(), Limit.of(txStatusInspectionLimitPerOrgPullSize)).forEach(tx -> {
            log.info("Checking transaction status for transaction: {}", tx.getId());
            L1SubmissionData l1SubmissionData1 = tx.getL1SubmissionData().orElseThrow(() -> new RuntimeException("Failed to get L1 submission data"));
            tx.setL1SubmissionData(Optional.of(updateL1SubmissionData(l1SubmissionData1, chainTip)));

            transactionEntityRepositoryGateway.storeTransaction(tx);
            log.info("Status updated for transaction: {}", tx.getId());
        });

        reportEntityRepositoryGateway.findDispatchedReportsThatAreNotFinalizedYet(org.getId(), Limit.of(txStatusInspectionLimitPerOrgPullSize)).forEach(report -> {
            log.info("Checking transaction status for report: {}", report.getId());
            L1SubmissionData l1SubmissionData = report.getL1SubmissionData().orElseThrow(() -> new RuntimeException("Failed to get L1 submission data"));
            report.setL1SubmissionData(Optional.of(updateL1SubmissionData(l1SubmissionData, chainTip)));

            reportEntityRepositoryGateway.storeReport(report);
            log.info("Status updated for report: {}", report.getId());
        });
    }

    private OnChainStatus getOnChainStatus(Optional<OnChainTxDetails> onChainTxDetails, Long txCreationSlot, ChainTip chainTip) {
        if (onChainTxDetails.isPresent()) {
            return new OnChainStatus(blockchainPublishStatusMapper.convert(onChainTxDetails.get().getFinalityScore()), onChainTxDetails.get().getFinalityScore());
        } else {
            // means tx is not on chain yet or was rolledback
            long txAgeInSlots = chainTip.getAbsoluteSlot() - txCreationSlot;
            // we have a grace period for rollback, this is to avoid premature rollbacks (e.g. when transaction is in the mempool still)
            boolean isRollbackReadyTimewise = txAgeInSlots > (rollbackGracePeriodMinutes * 60L);
            if (isRollbackReadyTimewise) {
                return new OnChainStatus(BlockchainPublishStatus.ROLLBACKED, FinalityScore.NONE);
            } else {
                // Is very low here correct? Should we have a different status e.g. NONE?
                return new OnChainStatus(BlockchainPublishStatus.SUBMITTED, FinalityScore.VERY_LOW);
            }
        }
    }

    private L1SubmissionData updateL1SubmissionData(L1SubmissionData submissionData, ChainTip chainTip) {
        Long txCreationSlot = submissionData.getCreationSlot().orElseThrow(() -> new RuntimeException("Failed to get tx creation slot"));
        String txHash = submissionData.getTransactionHash().orElseThrow(() -> new RuntimeException("Failed to get tx hash"));
        log.info("Checking transaction status changes for txHash:{}", txHash);
        Either<Problem, Optional<OnChainTxDetails>> txDetails = blockchainReaderPublicApi.getTxDetails(txHash);

        Optional<OnChainTxDetails> onChainTxDetails = txDetails.getOrElseThrow(() -> {
            log.error("Failed to get tx details for txHash:{}", txHash);
            return new RuntimeException("Failed to get tx details for txHash:" + txHash);
        });

        OnChainStatus onChainStatus = getOnChainStatus(onChainTxDetails, txCreationSlot, chainTip);

        if (onChainStatus.status().equals(BlockchainPublishStatus.ROLLBACKED)) {
            submissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.ROLLBACKED));
            submissionData.setCreationSlot(Optional.empty());
            submissionData.setAbsoluteSlot(Optional.empty());
            submissionData.setTransactionHash(Optional.empty());
            submissionData.setFinalityScore(Optional.empty());
        } else {
            submissionData.setFinalityScore(Optional.of(onChainStatus.finalityScore()));
            submissionData.setPublishStatus(Optional.of(onChainStatus.status()));
        }
        return submissionData;
    }

    private ChainTip getChainTip() {
        return blockchainReaderPublicApi.getChainTip().getOrElseThrow(() -> {
            log.error("Failed to get chain tip");
            return new RuntimeException("Failed to get chain tip");
        });
    }
}
