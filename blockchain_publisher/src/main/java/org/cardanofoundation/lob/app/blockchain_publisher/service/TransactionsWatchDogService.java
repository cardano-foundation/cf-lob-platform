package org.cardanofoundation.lob.app.blockchain_publisher.service;

import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApi;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.ROLLBACKED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionsWatchDogService {

    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final OrganisationPublicApiIF organisationPublicApiIF;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;
    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;
    private final BlockchainReaderPublicApi blockchainReaderPublicApi;

    @Value("${lob.blockchain_publisher.watchdog.rollback.grace.period.minutes:30}")
    @Getter
    @Setter
    private int rollbackGracePeriodMinutes = 30;

    @Value("${lob.blockchain_publisher.watchdog.rollbacks.enabled:true}")
    @Getter
    @Setter
    private boolean rollbackSupportEnabled = true;

    @PostConstruct
    public void init() {
        log.info("TransactionsWatchDogService configuration: rollbackGracePeriodMinutes={}, rollbacksEnabled={}", rollbackGracePeriodMinutes, rollbackSupportEnabled);

        log.info("TransactionsWatchDogService started");
    }

    @Transactional
    public List<Either<Problem, Set<TransactionEntity>>> checkTransactionStatusesForOrganisation(int limitPerOrg) {
        return organisationPublicApiIF.listAll()
                .stream()
                .map(org -> inspectOrganisationTransactions(org.getId(), limitPerOrg))
                .toList();
    }

    @Transactional
    public Either<Problem, Set<TransactionEntity>> inspectOrganisationTransactions(String organisationId, int limitPerOrg) {
        log.info("Polling for to check for transaction statuses...");

        val chainTipE = blockchainReaderPublicApi.getChainTip();
        if (chainTipE.isEmpty()) {
            log.error("Could not get cardano chain tip, exiting...");
            return Either.left(Problem.builder()
                    .withTitle("CHAIN_TIP_NOT_FOUND")
                    .withDetail("Could not get cardano chain tip")
                    .withStatus(Status.INTERNAL_SERVER_ERROR)
                    .with("organisationId", organisationId)
                    .with("reason", chainTipE.getLeft().getDetail())
                    .build()
            );
        }
        val chainTip = chainTipE.get();
        if (!chainTip.isSynced()) {
            log.warn("Chain tip is not synced, exiting...");

            return Either.right(Set.of());
        }
        val chainTipSlot = chainTip.getAbsoluteSlot();

        val rollbackEndangeredTransactions = transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(organisationId, Limit.of(limitPerOrg));
        log.info("Found {} transactions that are not finalised yet.", rollbackEndangeredTransactions.size());

        val txEntitiesSetE = new LinkedHashSet<Either<Problem, Optional<TransactionEntity>>>();

        for (val tx : rollbackEndangeredTransactions) {
            log.info("Transaction with id: {} is not finalised yet", tx.getId());

            try {
                txEntitiesSetE.add(checkTransactionStatusAndUpdateInDb(tx, chainTip));
            } catch (Exception e) {
                log.error("Problem while updating transaction status, issue: {}", e.getMessage());
            }
        }

        txEntitiesSetE.stream().filter(Either::isLeft).map(Either::getLeft).forEach(problem -> {
            log.error("Problem while updating transaction status, issue: {}", problem);
        });

        val successfullyUpdatedTxEntities = txEntitiesSetE
                .stream()
                .filter(Either::isRight)
                .map(Either::get)
                .filter(Optional::isPresent)
                .map(opt -> opt.orElseThrow())
                .collect(Collectors.toSet());

        // notify accounting core about updated transactions
        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents(organisationId, successfullyUpdatedTxEntities);

        return Either.right(successfullyUpdatedTxEntities);
    }

    @Transactional(propagation = REQUIRES_NEW)
    private Either<Problem, Optional<TransactionEntity>> checkTransactionStatusAndUpdateInDb(TransactionEntity txEntity,
                                                                                             ChainTip chainTip) {
        log.info("Checking transaction status for txId:{}", txEntity.getId());
        val txUpdateRequestE = checkTransactionStatusChange(txEntity, chainTip);
        if (txUpdateRequestE.isLeft()) {
            return Either.left(txUpdateRequestE.getLeft());
        }
        val txUpdateRequest = txUpdateRequestE.get();
        val tx = txUpdateRequest.getTransactionEntity();

        return switch (txUpdateRequest.getTransactionUpdateType()) {
            case NONE -> {
                log.info("Transaction with id: {} has no status change", tx.getId());

                yield Either.right(Optional.<TransactionEntity>empty()); // no changes to the entity since status has not been changed
            }
            case FINALITY_PROGRESSED -> {
                val cardanoFinalityScore = txUpdateRequest.finalityScore.orElseThrow();
                val blockchainPublishStatus = txUpdateRequest.blockchainPublishStatus.orElseThrow();

                log.info("Updating transaction with id: {} to status: {}, finality: {}", tx.getId(), blockchainPublishStatus, cardanoFinalityScore);

                tx.setL1SubmissionData(tx.getL1SubmissionData().map(l1SubmissionData -> {
                    val newBlockchainPublishStatusM = Optional.of(blockchainPublishStatus);
                    val newCardanoFinalityScoreM = Optional.of(cardanoFinalityScore);
                    val txAbsoluteSlotM = Optional.of(txUpdateRequest.absoluteSlot.orElseThrow());

                    l1SubmissionData.setAbsoluteSlot(txAbsoluteSlotM);
                    l1SubmissionData.setPublishStatus(newBlockchainPublishStatusM);
                    l1SubmissionData.setFinalityScore(newCardanoFinalityScoreM);

                    return l1SubmissionData;
                }));

                transactionEntityRepositoryGateway.storeTransaction(txUpdateRequest.getTransactionEntity());

                yield Either.right(Optional.of(txUpdateRequest.transactionEntity));
            }
            case ROLLBACKED -> {
                val blockchainPublishStatus = txUpdateRequest.getBlockchainPublishStatus().orElseThrow();

                log.info("Updating transaction with id: {} to blockchain publish status: {}", tx.getId(), blockchainPublishStatus);

                tx.setL1SubmissionData(tx.getL1SubmissionData().map(l1SubmissionData -> {
                    l1SubmissionData.setPublishStatus(Optional.of(blockchainPublishStatus));
                    l1SubmissionData.setCreationSlot(Optional.empty());
                    l1SubmissionData.setAbsoluteSlot(Optional.empty());
                    l1SubmissionData.setTransactionHash(Optional.empty());
                    l1SubmissionData.setFinalityScore(Optional.empty());

                    return l1SubmissionData;
                }));

                transactionEntityRepositoryGateway.storeTransaction(txUpdateRequest.getTransactionEntity());

                yield Either.right(Optional.of(txUpdateRequest.transactionEntity));
            }
        };
    }

    @Transactional(propagation = SUPPORTS)
    private Either<Problem, TransactionUpdateCommand> checkTransactionStatusChange(TransactionEntity tx,
                                                                                   ChainTip chainTip) {
        val txId = tx.getId();
        val l1SubmissionDataM = tx.getL1SubmissionData();
        if (l1SubmissionDataM.isEmpty() || l1SubmissionDataM.get().getCreationSlot().isEmpty()) {
            log.warn("Transaction with id: {} has no L1 submission data, this should never happen", txId);

            return Either.left(Problem.builder()
                    .withTitle("TRANSACTION_HAS_NO_L1_SUBMISSION_DATA")
                    .withDetail(STR."Transaction with id:\{txId} has no L1 submission data")
                    .withStatus(Status.INTERNAL_SERVER_ERROR)
                    .with("transactionId", txId)
                    .build()
            );
        }
        val l1SubmissionData = l1SubmissionDataM.orElseThrow();
        val txCreationSlot = l1SubmissionData.getCreationSlot().orElseThrow();

        val txHash = l1SubmissionData.getTransactionHash().orElseThrow();
        log.info("Checking transaction status changes for txId:{}", txHash);

        val cardanoOnChainDataE = blockchainReaderPublicApi.getTxDetails(txHash);
        if (cardanoOnChainDataE.isLeft()) {
            return Either.left(cardanoOnChainDataE.getLeft());
        }
        val cardanoOnChainDataM = cardanoOnChainDataE.get();

        // means that transaction is visible on chain
        if (cardanoOnChainDataM.isPresent()) {
            log.info("Found transaction with id: {} on chain", txHash);
            val cardanoOnChainData = cardanoOnChainDataM.orElseThrow();
            val txSubmissionAbsoluteSlot = cardanoOnChainData.getAbsoluteSlot();
            val chainTipAbsoluteSlot = chainTip.getAbsoluteSlot();
            val txAbsoluteSlot = cardanoOnChainData.getAbsoluteSlot();

            val cardanoFinalityScore = cardanoOnChainData.getFinalityScore();
            val blockchainPublishStatus = blockchainPublishStatusMapper.convert(cardanoFinalityScore);

            // check if new status is different than the one we have in the db
            if (l1SubmissionData.getPublishStatus().isPresent() && l1SubmissionData.getPublishStatus().orElseThrow().equals(blockchainPublishStatus)
                    && l1SubmissionData.getFinalityScore().isPresent() && l1SubmissionData.getFinalityScore().orElseThrow().equals(cardanoFinalityScore)) {
                log.info("Transaction with id: {} has no status change", txId);

                return Either.right(TransactionUpdateCommand.empty(tx));
            }

            return Either.right(TransactionUpdateCommand.of(tx, txAbsoluteSlot, blockchainPublishStatus, cardanoFinalityScore));
        }

        val txAgeInSlots = chainTip.getAbsoluteSlot() - txCreationSlot;
        // we have a grace period for rollback, this is to avoid premature rollbacks (e.g. when transaction is in the mempool still)
        val isRollbackReadyTimewise = txAgeInSlots > gracePeriodInSeconds(rollbackGracePeriodMinutes);

        // this means rollback scenario since we have L1 submission data but no on chain data
        // we do not want to prematurely raise ROLLBACK, so we have a grade period but if we are past it, we can safely rollback
        if (cardanoOnChainDataM.isEmpty() && isRollbackReadyTimewise && rollbackSupportEnabled) {
            log.warn("Transaction with id: {} is not on chain anymore, rollback?", txId);

            val txUpdate = new TransactionUpdateCommand(tx,
                    Optional.of(ROLLBACKED),
                    Optional.empty(),
                    Optional.empty(),
                    TransactionUpdateType.ROLLBACKED
            );

            return Either.right(txUpdate);
        }

        return Either.right(TransactionUpdateCommand.empty(tx));
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    private static class TransactionUpdateCommand {

        private final TransactionEntity transactionEntity;
        private final Optional<BlockchainPublishStatus> blockchainPublishStatus;
        private final Optional<FinalityScore> finalityScore;
        private final Optional<Long> absoluteSlot;
        private final TransactionUpdateType transactionUpdateType;

        public static TransactionUpdateCommand empty(TransactionEntity transactionEntity) {
            return new TransactionUpdateCommand(transactionEntity, Optional.empty(), Optional.empty(), Optional.empty(), TransactionUpdateType.NONE);
        }

        public static TransactionUpdateCommand of(TransactionEntity transactionEntity,
                                                  long absoluteSlot,
                                                  BlockchainPublishStatus changedStatus,
                                                  FinalityScore finalityScore) {
            return new TransactionUpdateCommand(
                    transactionEntity,
                    Optional.of(changedStatus),
                    Optional.of(finalityScore),
                    Optional.of(absoluteSlot),
                    TransactionUpdateType.FINALITY_PROGRESSED
            );
        }

    }

    private enum TransactionUpdateType {
        NONE,
        FINALITY_PROGRESSED,  // this means that transaction is still on chain and we have newer finality score / assurance level available, we just need to update it
        ROLLBACKED // this means that transaction is not on chain anymore, we need to update the status to ROLLBACKED
    }

    private int gracePeriodInSeconds(int gracePeriodInMinutes) {
        return gracePeriodInMinutes * 60;
    }

}
