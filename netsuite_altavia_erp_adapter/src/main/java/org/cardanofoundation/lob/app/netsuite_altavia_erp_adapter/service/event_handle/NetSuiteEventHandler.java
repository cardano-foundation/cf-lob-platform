package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.event_handle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ScheduledReconcilationEvent;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.NetSuiteExtractionService;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.NetSuiteReconcilationService;
import org.springframework.modulith.events.ApplicationModuleListener;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteEventHandler {

    private final NetSuiteExtractionService netSuiteExtractionService;
    private final NetSuiteReconcilationService netSuiteReconcilationService;

    @ApplicationModuleListener
    public void handleScheduledIngestionEvent(ScheduledIngestionEvent event) {
        log.info("Handling handleScheduledIngestionEvent...");

        netSuiteExtractionService.startNewERPExtraction(
                event.getOrganisationId(),
                event.getInitiator(),
                event.getUserExtractionParameters()
        );

        log.info("Handled handleScheduledIngestionEvent.");
    }

    @ApplicationModuleListener
    public void handleTransactionBatchCreatedEvent(TransactionBatchCreatedEvent transactionBatchCreatedEvent) {
        log.info("Handling handleTransactionBatchCreatedEvent...");

        netSuiteExtractionService.continueERPExtraction(
                transactionBatchCreatedEvent.getBatchId(),
                transactionBatchCreatedEvent.getOrganisationId(),
                transactionBatchCreatedEvent.getAdapterInstanceId(),
                transactionBatchCreatedEvent.getUserExtractionParameters(),
                transactionBatchCreatedEvent.getSystemExtractionParameters()
        );

        log.info("Handled handleTransactionBatchCreatedEvent.");
    }

    @ApplicationModuleListener
    public void handleScheduledReconciliationEvent(ScheduledReconcilationEvent scheduledReconcilationEvent) {
        log.info("Handling handleScheduledReconciliationEvent...");

        netSuiteReconcilationService.startERPReconcilation(
                scheduledReconcilationEvent.getOrganisationId(),
                scheduledReconcilationEvent.getInitiator(),
                scheduledReconcilationEvent.getFrom(),
                scheduledReconcilationEvent.getTo()
        );

        log.info("Handled handleScheduledReconciliationEvent.");
    }

    @ApplicationModuleListener
    public void handleCreatedReconciliationEvent(ReconcilationCreatedEvent reconcilationCreatedEvent) {
        log.info("Handling handleCreatedReconciliationEvent...");

        netSuiteReconcilationService.continueReconcilation(
                reconcilationCreatedEvent.getReconciliationId(),
                reconcilationCreatedEvent.getOrganisationId(),
                reconcilationCreatedEvent.getAdapterInstanceId(),
                reconcilationCreatedEvent.getFrom(),
                reconcilationCreatedEvent.getTo()
        );

        log.info("Handled handleCreatedReconciliationEvent.");
    }

}
