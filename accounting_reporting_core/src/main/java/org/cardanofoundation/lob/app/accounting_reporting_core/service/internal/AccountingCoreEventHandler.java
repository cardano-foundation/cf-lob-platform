package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchStartedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;
    private final TransactionConverter transactionConverter;
    private final LedgerService ledgerService;
    private final TransactionBatchService transactionBatchService;

    @ApplicationModuleListener
    public void handleTransactionBatchFailedEvent(TransactionBatchFailedEvent event) {
        log.info("Received handleTransactionBatchFailedEvent event, event: {}", event);

        val error = event.getError();

        transactionBatchService.failTransactionBatch(
                event.getBatchId(),
                event.getUserExtractionParameters(),
                event.getSystemExtractionParameters(),
                error);

        log.info("Finished processing handleTransactionBatchFailedEvent event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleTransactionBatchStartedEvent(TransactionBatchStartedEvent event) {
        log.info("Received handleTransactionBatchStartedEvent event, event: {}", event);

        erpIncomingDataProcessor.initiateIngestion(event);

        log.info("Finished processing handleTransactionBatchStartedEvent event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleTransactionBatchChunkEvent(TransactionBatchChunkEvent transactionBatchChunkEvent) {
        String batchId = transactionBatchChunkEvent.getBatchId();

        log.info("Received handleTransactionBatchChunkEvent event...., event, batch_id: {}, chunk_size:{}", batchId, transactionBatchChunkEvent.getTransactions().size());

        val txs = transactionBatchChunkEvent.getTransactions();
        val detachedDbTxs = transactionConverter.convertToDbDetached(txs);

        erpIncomingDataProcessor.continueIngestion(
                transactionBatchChunkEvent.getOrganisationId(),
                batchId,
                transactionBatchChunkEvent.getTotalTransactionsCount(),
                detachedDbTxs,
                ProcessorFlags.builder()
                        .reprocess(false)
                        .build()
        );

        log.info("Finished processing handleTransactionBatchChunkEvent event...., event, batch_id: {}", batchId);
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received handleLedgerUpdatedEvent event, event: {}", event.getStatusUpdates());

        val txStatusUpdatesMap = event.statusUpdatesMap();

        ledgerService.updateTransactionsWithNewStatuses(txStatusUpdatesMap);
        transactionBatchService.updateBatchesPerTransactions(txStatusUpdatesMap);

        log.info("Finished processing handleLedgerUpdatedEvent event, event: {}", event.getStatusUpdates());
    }

}
