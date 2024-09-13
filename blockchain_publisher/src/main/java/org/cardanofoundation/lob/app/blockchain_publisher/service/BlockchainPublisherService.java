package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.BlockchainPublisherException;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service("blockchain_publisher.blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;
    private final TransactionConverter transactionConverter;

    @Transactional
    public void storeTransactionsForDispatchLater(String organisationId,
                                                 Set<Transaction> transactions) {
        log.info("dispatchTransactionsToBlockchains..., orgId:{}", organisationId);

        val updatedTxs = new HashSet<TransactionEntity>();
        var exception = false;
        for (val transaction : transactions) {
            try {
                storeTransactionForDispatchLater(organisationId, transaction).ifPresent(updatedTxs::add);
            } catch (Exception e) {
                log.error("Error while storing transaction for dispatch later, orgId: {}, txId:{}", organisationId, transaction.getId(), e);
                exception = true;
            }
        }

        // we want to avoid many events flowing but updating in 1 LOB transaction = 1 db transaction
        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents(organisationId, updatedTxs);

        if (exception) {
            // will be stored in unpublished events
            throw new BlockchainPublisherException(STR."Error while storing transaction for dispatch later, some transactions may have been processe, total: \{transactions.size()}, updated: \{updatedTxs.size()}");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<TransactionEntity> storeTransactionForDispatchLater(String organisationId, Transaction transaction) {
        log.info("storeTransactionForDispatchLater..., orgId: {}, txId:{}", organisationId, transaction.getId());

        val tx = transactionConverter.convertToDbDetached(transaction);

        // idempotent receiver
        val storedOnlyNewTransactionM = transactionEntityRepositoryGateway.storeOnlyNewTransaction(tx);
        if (storedOnlyNewTransactionM.isEmpty()) {
            return Optional.empty();
        }
        val storedOnlyNewTransaction = storedOnlyNewTransactionM.orElseThrow();

        return Optional.of(storedOnlyNewTransaction);
    }

}
