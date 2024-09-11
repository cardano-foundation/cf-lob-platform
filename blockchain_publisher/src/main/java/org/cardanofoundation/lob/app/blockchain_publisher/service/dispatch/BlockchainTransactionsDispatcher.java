package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import com.bloxbean.cardano.client.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactions;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
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
import java.util.concurrent.TimeoutException;

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
    private final DispatchingStrategy dispatchingStrategy;

    @Value("${lob.blockchain.publisher.dispatcher.pullBatchSize:50}")
    private int pullTransactionsBatchSize = 50;

    @Transactional
    public void dispatchTransactions() {
        log.info("Dispatching passedTransactions to the cardano blockchain...");

        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.getId();
            val transactionsBatch = transactionEntityRepositoryGateway.findTransactionsByStatus(organisationId, pullTransactionsBatchSize);
            val transactionToDispatch = dispatchingStrategy.apply(organisationId, transactionsBatch);

            if (!transactionToDispatch.isEmpty()) {
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

        var serialisedTxE = l1TransactionCreator.pullBlockchainTransaction(organisationId, transactions);

        if (serialisedTxE.isEmpty()) {
            log.warn("Error, there is more passedTransactions to dispatch for organisation:{}", organisationId);

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
        } catch (InterruptedException | TimeoutException | ApiException e) {
            log.error("Error sending transaction on chain and / or updating db", e);
        }

        return Optional.empty();
    }

    @Transactional
    private void sendTransactionOnChainAndUpdateDb(BlockchainTransactions blockchainTransactions) throws InterruptedException, TimeoutException, ApiException {
        val txData = blockchainTransactions.serialisedTxData();
        val l1SubmissionData = transactionSubmissionService.submitTransaction(txData);
        val organisationId = blockchainTransactions.organisationId();
        val allTxs = blockchainTransactions.submittedTransactions();

        updateTransactionStatuses(l1SubmissionData, blockchainTransactions);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents(organisationId, allTxs);

        log.info("Blockchain transaction submitted, l1SubmissionData:{}", l1SubmissionData);
    }

    @Transactional
    private void updateTransactionStatuses(String txHash,
                                           BlockchainTransactions blockchainTransactions) {
        for (val txEntity : blockchainTransactions.submittedTransactions()) {
            txEntity.setL1SubmissionData(Optional.of(L1SubmissionData.builder()
                    .transactionHash(txHash)
                    .creationSlot(blockchainTransactions.creationSlot())
                    .publishStatus(SUBMITTED)
                    .build())
            );

            transactionEntityRepositoryGateway.storeTransaction(txEntity);
        }
    }

}
