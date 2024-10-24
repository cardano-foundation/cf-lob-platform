package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreTransactionRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final AccountingCoreTransactionRepository accountingCoreTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionConverter transactionConverter;
    private final PIIDataFilteringService piiDataFilteringService;
    private final OrganisationPublicApi organisationPublicApi;

    @Value("${ledger.dispatch.batch.size:100}")
    private int dispatchBatchSize;

    @Transactional
    public void updateTransactionsWithNewStatuses(Map<String, TxStatusUpdate> statuses) {
        log.info("Updating dispatch status for statusMapCount: {}", statuses.size());

        val txIds = statuses.keySet();

        val transactionEntities = accountingCoreTransactionRepository.findAllById(txIds);

        for (val tx : transactionEntities) {
            val txStatusUpdate = statuses.get(tx.getId());
            tx.setLedgerDispatchStatus(txStatusUpdate.getStatus());
        }

        accountingCoreTransactionRepository.saveAll(transactionEntities);

        log.info("Updated dispatch status for statusMapCount: {} completed.", statuses.size());
    }

    @Transactional
    public void checkIfThereAreTransactionsToDispatch(String organisationId,
                                                      Set<TransactionEntity> transactions) {
        val txIds = transactions.stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());

        log.info("dispatchTransactionToBlockchainPublisher, txIds: {}", txIds);

        val dispatchPendingTransactions = transactions.stream()
                .filter(TransactionEntity::isDispatchable)
                .collect(Collectors.toSet());

        dispatchTransactions(organisationId, dispatchPendingTransactions);
    }

    @Transactional
    public void dispatchPending(int limit) {
        for (val organisation : organisationPublicApi.listAll()) {
            val dispatchTransactions = accountingCoreTransactionRepository.findDispatchableTransactions(organisation.getId(), Limit.of(limit));

            log.info("dispatchPending, organisationId: {}, total tx count: {}", organisation.getId(), dispatchTransactions.size());

            dispatchTransactions(organisation.getId(), dispatchTransactions);
        }
    }

    @Transactional(propagation = SUPPORTS)
    public void dispatchTransactions(String organisationId,
                                     Set<TransactionEntity> transactions) {
        log.info("dispatchTransactionToBlockchainPublisher, total tx count: {}", transactions.size());

        if (transactions.isEmpty()) {
            return;
        }

        val canonicalTxs = transactionConverter.convertFromDb(transactions);
        val piiFilteredOutTransactions = piiDataFilteringService.apply(canonicalTxs);

        for (val partition : Partitions.partition(piiFilteredOutTransactions, dispatchBatchSize)) {
            val txs = partition.asSet();

            log.info("dispatchTransactionToBlockchainPublisher, partitionSize: {}", txs.size());

            applicationEventPublisher.publishEvent(LedgerUpdateCommand.create(
                    EventMetadata.create(LedgerUpdateCommand.VERSION),
                    organisationId,
                    txs)
            );
        }
    }

}
