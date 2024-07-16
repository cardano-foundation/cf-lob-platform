package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.LedgerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("accounting_core.TransactionDispatcherJob")
@Slf4j
@RequiredArgsConstructor
public class TransactionDispatcherJob {
    private final LedgerService ledgerService;

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT10S")
    public void execute() {
        log.info("Executing PeriodicTxDispatcherJob...");

        ledgerService.dispatchPending(10_000);

        log.info("Finished executing PeriodicTxDispatcherJob.");
    }

}
