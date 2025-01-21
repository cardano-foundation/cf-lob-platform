package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import com.bloxbean.cardano.client.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.API3BlockchainTransaction;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.ReportEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.API3L1TransactionCreator;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.TransactionSubmissionService;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.SUBMITTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainReportsDispatcher {

    private final OrganisationPublicApi organisationPublicApi;
    private final ReportEntityRepositoryGateway reportEntityRepositoryGateway;
    private final DispatchingStrategy<ReportEntity> dispatchingStrategy = new ImmediateDispatchingStrategy<>();
    private final API3L1TransactionCreator api3L1TransactionCreator;
    private final TransactionSubmissionService transactionSubmissionService;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Value("${lob.blockchain_publisher.dispatcher.pullBatchSize:50}")
    private int pullTransactionsBatchSize = 50;

    @Transactional
    public void dispatchReports() {
        log.info("Pooling for blockchain reports to be send to the blockchain...");

        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.getId();

            val reports = reportEntityRepositoryGateway.findReportsByStatus(organisationId, pullTransactionsBatchSize);
            val reportsCount = reports.size();

            log.info("Dispatching reports for organisationId: {}, report count:{}", organisationId, reportsCount);

            if (reportsCount > 0) {
                val toDispatch = dispatchingStrategy.apply(organisationId, reports);

                dispatchReports(organisationId, toDispatch);
            }
        }

        log.info("Pooling for blockchain reports to be send to the blockchain...done");
    }

    @Transactional
    protected void dispatchReports(String organisationId,
                                   Set<ReportEntity> reportEntities) {
        log.info("Dispatching reports for organisation: {}", organisationId);

        for (val reportEntity : reportEntities) {
            dispatchReport(organisationId, reportEntity);
        }
    }

    @Transactional
    public void dispatchReport(String organisationId, ReportEntity reportEntity) {
        log.info("Dispatching report for organisation: {}", organisationId);

        val api3BlockchainTransactionE = createAndSendBlockchainTransactions(reportEntity);
        if (api3BlockchainTransactionE.isEmpty()) {
            log.info("No more reports to dispatch for organisationId, success or error?, organisationId: {}", organisationId);
        }
    }

    @Transactional
    private Optional<API3BlockchainTransaction> createAndSendBlockchainTransactions(ReportEntity reportEntity) {
        log.info("Creating and sending blockchain transactions for report:{}", reportEntity.getReportId());

        val serialisedTxE = api3L1TransactionCreator.pullBlockchainTransaction(reportEntity);

        if (serialisedTxE.isLeft()) {
            val problem = serialisedTxE.getLeft();

            log.error("Error pulling blockchain transaction, problem: {}", problem);

            return Optional.empty();
        }

        val serialisedTx = serialisedTxE.get();
        try {
            sendTransactionOnChainAndUpdateDb(serialisedTx);

            return Optional.of(serialisedTx);
        } catch (InterruptedException | ApiException e) {
            log.error("Error sending transaction on chain and / or updating db", e);
        }

        return Optional.empty();
    }

    @Transactional
    private void sendTransactionOnChainAndUpdateDb(API3BlockchainTransaction api3BlockchainTransaction) throws InterruptedException, ApiException {
        val reportTxData = api3BlockchainTransaction.serialisedTxData();

        val l1SubmissionData = transactionSubmissionService.submitTransactionWithPossibleConfirmation(reportTxData);

        val txHash = l1SubmissionData.txHash();
        val txAbsoluteSlotM = l1SubmissionData.absoluteSlot();

        val report = api3BlockchainTransaction.report();
        val creationSlot = api3BlockchainTransaction.creationSlot();

        updateTransactionStatuses(txHash, txAbsoluteSlotM, creationSlot, report);
        ledgerUpdatedEventPublisher.sendReportLedgerUpdatedEvents(report.getOrganisation().getId(), Set.of(report));

        log.info("Blockchain transaction submitted (report), l1SubmissionData:{}", l1SubmissionData);
    }

    @Transactional
    private void updateTransactionStatuses(String txHash,
                                           Optional<Long> absoluteSlot,
                                           long creationSlot,
                                           ReportEntity reportEntity) {

        reportEntity.setL1SubmissionData(Optional.of(L1SubmissionData.builder()
                    .transactionHash(txHash)
                    .absoluteSlot(absoluteSlot.orElse(null)) // if tx is not confirmed yet, slot will not be available
                    .creationSlot(creationSlot)
                    .publishStatus(SUBMITTED)
                    .build())
            );

        reportEntityRepositoryGateway.storeReport(reportEntity);
    }

}
