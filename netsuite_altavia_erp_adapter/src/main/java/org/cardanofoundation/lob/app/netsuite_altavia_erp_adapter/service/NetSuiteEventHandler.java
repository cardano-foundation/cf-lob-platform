package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchCreatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteEventHandler {

    private final NetSuiteService netSuiteService;

    @ApplicationModuleListener
    public void handleScheduledIngestionEvent(ScheduledIngestionEvent event) {
        log.info("Handling handleScheduledIngestionEvent...");

        netSuiteService.startNewERPExtraction(
                event.getOrganisationId(),
                event.getInitiator(),
                event.getUserExtractionParameters()
        );

        log.info("Handled handleScheduledIngestionEvent.");
    }

    @ApplicationModuleListener
    public void handleTransactionBatchCreatedEvent(TransactionBatchCreatedEvent transactionBatchCreatedEvent) {
        log.info("Handling handleTransactionBatchCreatedEvent...");

        netSuiteService.continueERPExtraction(
                transactionBatchCreatedEvent.getBatchId(),
                transactionBatchCreatedEvent.getOrganisationId(),
                transactionBatchCreatedEvent.getInstanceId(),
                transactionBatchCreatedEvent.getUserExtractionParameters(),
                transactionBatchCreatedEvent.getSystemExtractionParameters()
        );

        log.info("Handled handleTransactionBatchCreatedEvent.");
    }

}
