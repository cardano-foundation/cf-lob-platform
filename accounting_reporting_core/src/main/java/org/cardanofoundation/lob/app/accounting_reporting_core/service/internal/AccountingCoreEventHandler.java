package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchStartedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationFinalisationEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationStartedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.cardanofoundation.lob.app.support.modulith.SyncApplicationModuleListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;
    private final TransactionConverter transactionConverter;
    private final LedgerService ledgerService;
    private final TransactionBatchService transactionBatchService;
    private final TransactionReconcilationService transactionReconcilationService;

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received handleLedgerUpdatedEvent event, event: {}", event.getStatusUpdates());

        val txStatusUpdatesMap = event.statusUpdatesMap();

        ledgerService.updateTransactionsWithNewStatuses(txStatusUpdatesMap);
        transactionBatchService.updateBatchesPerTransactions(txStatusUpdatesMap);

        log.info("Finished processing handleLedgerUpdatedEvent event, event: {}", event.getStatusUpdates());
    }

    @ApplicationModuleListener
    public void handleTransactionBatchFailedEvent(TransactionBatchFailedEvent event) {
        log.info("Received handleTransactionBatchFailedEvent event, event: {}", event);

        val error = event.getError();

        transactionBatchService.failTransactionBatch(
                event.getBatchId(),
                event.getUserExtractionParameters(),
                event.getSystemExtractionParameters(),
                error
        );

        log.info("Finished processing handleTransactionBatchFailedEvent event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleTransactionBatchStartedEvent(TransactionBatchStartedEvent event) {
        log.info("Received handleTransactionBatchStartedEvent event, event: {}", event);

        erpIncomingDataProcessor.initiateIngestion(
                event.getBatchId(),
                event.getOrganisationId(),
                event.getUserExtractionParameters(),
                event.getSystemExtractionParameters()
        );

        log.info("Finished processing handleTransactionBatchStartedEvent event, event: {}", event);
    }

    @SyncApplicationModuleListener // we need a sync process to avoid out of order events
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
                new ProcessorFlags(ProcessorFlags.Trigger.IMPORT)
        );

        log.info("Finished processing handleTransactionBatchChunkEvent event...., event, batch_id: {}", batchId);
    }


    @ApplicationModuleListener
    public void handleReconcilationChunkFailedEvent(ReconcilationFailedEvent event) {
        log.info("Received handleReconcilationChunkFailedEvent event, event: {}", event);

        transactionReconcilationService.failReconcilation(
                event.getReconciliationId(),
                event.getOrganisationId(),
                Optional.empty(),
                Optional.empty(),
                event.getError()
        );

        log.info("Finished processing handleReconcilationChunkFailedEvent event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleReconcilationStartedEvent(ReconcilationStartedEvent event) {
        log.info("Received handleReconcilationStartedEvent, event: {}", event);

        erpIncomingDataProcessor.initiateReconcilation(event);

        log.info("Finished processing handleReconcilationStartedEvent, event: {}", event);
    }

    @SyncApplicationModuleListener // we need a sync process to avoid out of order events
    public void handleReconcilationChunkEvent(ReconcilationChunkEvent event) {
        log.info("Received handleReconcilationChunkEvent, event: {}", event);

        val reconcilationId = event.getReconciliationId();
        val organisationId = event.getOrganisationId();
        val fromDate = event.getFrom();
        val toDate = event.getTo();
        val transactions = event.getTransactions();
        val chunkDetachedTxEntities = transactionConverter.convertToDbDetached(transactions);

        erpIncomingDataProcessor.continueReconcilation(
                reconcilationId,
                organisationId,
                fromDate,
                toDate,
                chunkDetachedTxEntities
        );

        log.info("Finished processing handleReconcilationChunkEvent, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleReconcilationFinalisation(ReconcilationFinalisationEvent event) {
        log.info("Received handleReconcilationFinalisation, event: {}", event);

        erpIncomingDataProcessor.finialiseReconcilation(event);

        log.info("Finished processing handleReconcilationFinalisation, event: {}", event);
    }

}
