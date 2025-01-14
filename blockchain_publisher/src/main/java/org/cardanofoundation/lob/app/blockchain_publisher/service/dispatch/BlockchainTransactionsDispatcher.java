package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import com.bloxbean.cardano.client.api.exception.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactions;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.L1TransactionCreator;
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
public class BlockchainTransactionsDispatcher {

    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final OrganisationPublicApi organisationPublicApi;
    private final L1TransactionCreator l1TransactionCreator;
    private final TransactionSubmissionService transactionSubmissionService;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;
    private final DispatchingStrategy<TransactionEntity> dispatchingStrategy;

    @Value("${lob.blockchain_publisher.dispatcher.pullBatchSize:50}")
    private int pullTransactionsBatchSize = 50;

    @PostConstruct
    public void init() {
        log.info("BlockchainTransactionsDispatcher initialized with pullTransactionsBatchSize:{}", pullTransactionsBatchSize);
        log.info("DispatchStrategy:{}", dispatchingStrategy.getClass().getSimpleName());
    }

    @Transactional
    public void dispatchTransactions() {
        log.info("Dispatching txs to the cardano blockchain...");

        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.getId();
            val transactionsBatch = transactionEntityRepositoryGateway.findTransactionsByStatus(organisationId, pullTransactionsBatchSize);
            val transactionToDispatch = dispatchingStrategy.apply(organisationId, transactionsBatch);

            val dispatchTxCount = transactionToDispatch.size();
            log.info("Dispatching txs for organisationId:{}, tx count:{}", organisationId, dispatchTxCount);
            if (dispatchTxCount > 0) {
                dispatchTransactionsBatch(organisationId, transactionToDispatch);
            }
        }
    }

    @Transactional
    protected void dispatchTransactionsBatch(String organisationId,
                                             Set<TransactionEntity> transactionEntitiesBatch) {
        log.info("Dispatching passedTransactions for organisation: {}", organisationId);

        val blockchainTransactionsM = createAndSendBlockchainTransactions(organisationId, transactionEntitiesBatch);

        if (blockchainTransactionsM.isEmpty()) {
            log.info("No more passedTransactions to dispatch for organisationId: {}", organisationId);
            return;
        }

        val blockchainTransactions = blockchainTransactionsM.orElseThrow();

        val submittedTxCount = blockchainTransactions.submittedTransactions().size();
        val remainingTxCount = blockchainTransactions.remainingTransactions().size();

        log.info("Submitted tx count:{}, remainingTxCount:{}", submittedTxCount, remainingTxCount);
    }

    @Transactional
    private Optional<BlockchainTransactions> createAndSendBlockchainTransactions(String organisationId,
                                                                                 Set<TransactionEntity> transactions) {
        log.info("Processing passedTransactions for organisation:{}, remaining size:{}", organisationId, transactions.size());

        if (transactions.isEmpty()) {
            log.info("No more passedTransactions to dispatch for organisation:{}", organisationId);

            return Optional.empty();
        }

        val serialisedTxE = l1TransactionCreator.pullBlockchainTransaction(organisationId, transactions);
        if (serialisedTxE.isEmpty()) {
            log.warn("Error, there is more passedTransactions to dispatch for organisation:{}, actual issue:{}", organisationId, serialisedTxE.getLeft());

            return Optional.empty();
        }

        val serialisedTxM = serialisedTxE.get();

        if (serialisedTxM.isEmpty()) {
            log.warn("No passedTransactions to dispatch for organisationId:{}", organisationId);

            return Optional.empty();
        }

        val serialisedTx = serialisedTxM.orElseThrow();
        try {
            sendTransactionOnChainAndUpdateDb(serialisedTx);
        } catch (InterruptedException | ApiException e) {
            log.error("Error sending transaction on chain and / or updating db", e);
        }

        return Optional.empty();
    }

    @Transactional
    private void sendTransactionOnChainAndUpdateDb(BlockchainTransactions blockchainTransactions) throws InterruptedException, ApiException {
        val txData = blockchainTransactions.serialisedTxData();
        val l1SubmissionData = transactionSubmissionService.submitTransactionWithPossibleConfirmation(txData);
        val organisationId = blockchainTransactions.organisationId();
        val allTxs = blockchainTransactions.submittedTransactions();

        val txHash = l1SubmissionData.txHash();
        val txAbsoluteSlotM = l1SubmissionData.absoluteSlot();

        updateTransactionStatuses(txHash, txAbsoluteSlotM, blockchainTransactions);

        ledgerUpdatedEventPublisher.sendTxLedgerUpdatedEvents(organisationId, allTxs);

        log.info("Blockchain transaction submitted, l1SubmissionData:{}", l1SubmissionData);
    }

    @Transactional
    private void updateTransactionStatuses(String txHash,
                                           Optional<Long> absoluteSlot,
                                           BlockchainTransactions blockchainTransactions) {
        for (val txEntity : blockchainTransactions.submittedTransactions()) {
            txEntity.setL1SubmissionData(Optional.of(L1SubmissionData.builder()
                    .transactionHash(txHash)
                    .absoluteSlot(absoluteSlot.orElse(null)) // if tx is not confirmed yet, slot will not be available
                    .creationSlot(blockchainTransactions.creationSlot())
                    .publishStatus(SUBMITTED)
                    .build())
            );

            transactionEntityRepositoryGateway.storeTransaction(txEntity);
        }
    }

}
