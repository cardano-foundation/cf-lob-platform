package org.cardanofoundation.lob.app.blockchain_publisher.service.event_handle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublisherService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionConverter;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainPublisherEventHandler {

    private final TransactionConverter transactionConverter;
    private final BlockchainPublisherService blockchainPublisherService;

    // received when a ledger update command is published meaning accounting core has changed to the transaction status = MARK_DISPATCH
    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(LedgerUpdateCommand command) {
        log.info("Received LedgerUpdateCommand: {}", command);

        blockchainPublisherService.storeTransactionForDispatchLater(
                command.getOrganisationId(),
                command.getTransactions()
        );
    }

}
