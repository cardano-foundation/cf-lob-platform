package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service("blockchain_publisher.blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Transactional
    public void storeTransactionForDispatchLater(String organisationId,
                                                 Set<TransactionEntity> transactions) {
        log.info("dispatchTransactionsToBlockchains..., orgId:{}", organisationId);

        val storedTransactions = transactionEntityRepositoryGateway.storeOnlyNewTransactions(transactions);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents(organisationId, storedTransactions);
    }

}
