package org.cardanofoundation.lob.app.blockchain_publisher.service.event_handle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
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

    // received when a ledger update command is published meaning accounting core has changed to the transaction status
    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(LedgerUpdateCommand command) {
        log.info("Received LedgerUpdateCommand: {}", command);

        val organisationId = command.getOrganisationId();
        val transactions = command.getTransactions();

        val txs = transactionConverter.convertToDbDetached(transactions);

        blockchainPublisherService.storeTransactionForDispatchLater(organisationId, txs);
    }

}
