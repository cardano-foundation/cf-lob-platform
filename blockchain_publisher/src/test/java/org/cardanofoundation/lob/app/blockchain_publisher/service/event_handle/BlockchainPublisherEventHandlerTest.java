package org.cardanofoundation.lob.app.blockchain_publisher.service.event_handle;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublisherService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionConverter;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

class BlockchainPublisherEventHandlerTest {

    private BlockchainPublisherEventHandler blockchainPublisherEventHandler;

    @Mock
    private TransactionConverter transactionConverter;

    @Mock
    private BlockchainPublisherService blockchainPublisherService;

    @Captor
    private ArgumentCaptor<Set<Transaction>> transactionsCaptor;

    @Spy
    private Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        blockchainPublisherEventHandler = new BlockchainPublisherEventHandler(transactionConverter, blockchainPublisherService);
    }

    @Test
    void testHandleLedgerUpdateCommand() {
        // Given
        String organisationId = "org1";
        Set<Transaction> transactions = Set.of(createTransaction("tx1"), createTransaction("tx2"));
        LedgerUpdateCommand command = LedgerUpdateCommand.create(EventMetadata.create(LedgerUpdateCommand.VERSION), organisationId, transactions);

        // When
        blockchainPublisherEventHandler.handleLedgerUpdateCommand(command);

        // Then
        verify(blockchainPublisherService).storeTransactionsForDispatchLater(eq(organisationId), transactionsCaptor.capture());

        Set<Transaction> capturedTransactions = transactionsCaptor.getValue();

        assertThat(capturedTransactions).isEqualTo(transactions);
    }

    @Test
    void testHandleLedgerUpdateCommand_EmptyTransactions() {
        // Given
        String organisationId = "org1";
        Set<Transaction> transactions = Set.of();  // Empty set of transactions
        LedgerUpdateCommand command = LedgerUpdateCommand.create(EventMetadata.create(LedgerUpdateCommand.VERSION), organisationId, transactions);

        blockchainPublisherEventHandler.handleLedgerUpdateCommand(command);

        // Then
        verify(blockchainPublisherService).storeTransactionsForDispatchLater(eq(organisationId), transactionsCaptor.capture());

        Set<Transaction> capturedTransactions = transactionsCaptor.getValue();
        assertThat(capturedTransactions).isEqualTo(transactions);
    }

    private Transaction createTransaction(String transactionId) {
        return Transaction.builder()
                .id(transactionId)
                .internalTransactionNumber("intTxNum")
                .batchId("batchId")
                .entryDate(LocalDate.now())
                .transactionType(TransactionType.CardCharge)
                .organisation(Organisation.builder()
                        .id("orgId")
                        .name(Optional.of("orgName"))
                        .build())
                .accountingPeriod(YearMonth.now(clock))
                .build();
    }

}