package org.cardanofoundation.lob.app.blockchain_publisher.service.event_handle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.ReportLedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionLedgerUpdateCommand;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublisherService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainPublisherEventHandler {

    private final BlockchainPublisherService blockchainPublisherService;

    // received when a ledger update command is published meaning accounting core has changed to the transaction status = MARK_DISPATCH
    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(TransactionLedgerUpdateCommand command) {
        log.info("Received LedgerUpdateCommand: {}", command);

        blockchainPublisherService.storeTransactionForDispatchLater(
                command.getOrganisationId(),
                command.getTransactions()
        );
    }

    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(ReportLedgerUpdateCommand command) {
        log.info("Received ReportLedgerUpdateCommand: {}", command);

//        blockchainPublisherService.storeTransactionForDispatchLater(
//                command.getOrganisationId(),
//                command.getTransactions()
//        );

        blockchainPublisherService.storeReportsForDispatchLater(
                command.getOrganisationId(),
                command.getReports()
        );
    }

}
