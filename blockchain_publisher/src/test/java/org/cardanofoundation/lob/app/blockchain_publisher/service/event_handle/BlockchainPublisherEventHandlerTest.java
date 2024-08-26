package org.cardanofoundation.lob.app.blockchain_publisher.service.event_handle;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublisherService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BlockchainPublisherEventHandlerTest {

    private BlockchainPublisherEventHandler blockchainPublisherEventHandler;

    @Mock
    private TransactionConverter transactionConverter;

    @Mock
    private BlockchainPublisherService blockchainPublisherService;

    @Captor
    private ArgumentCaptor<Set<TransactionEntity>> transactionsCaptor;

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
        LedgerUpdateCommand command = LedgerUpdateCommand.create(organisationId, transactions);

        val txEntity1 = new TransactionEntity();
        txEntity1.setId("tx1");

        val txEntity2 = new TransactionEntity();
        txEntity2.setId("tx2");

        Set<TransactionEntity> convertedTransactions = Set.of(txEntity1, txEntity2);
        when(transactionConverter.convertToDbDetached(transactions)).thenReturn(convertedTransactions);

        // When
        blockchainPublisherEventHandler.handleLedgerUpdateCommand(command);

        // Then
        verify(transactionConverter).convertToDbDetached(transactions);
        verify(blockchainPublisherService).storeTransactionForDispatchLater(eq(organisationId), transactionsCaptor.capture());

        Set<TransactionEntity> capturedTransactions = transactionsCaptor.getValue();
        assertThat(capturedTransactions).isEqualTo(convertedTransactions);
    }

    @Test
    void testHandleLedgerUpdateCommand_EmptyTransactions() {
        // Given
        String organisationId = "org1";
        Set<Transaction> transactions = Set.of();  // Empty set of transactions
        LedgerUpdateCommand command = LedgerUpdateCommand.create(organisationId, transactions);

        Set<TransactionEntity> convertedTransactions = Set.of();
        when(transactionConverter.convertToDbDetached(transactions)).thenReturn(convertedTransactions);

        // When
        blockchainPublisherEventHandler.handleLedgerUpdateCommand(command);

        // Then
        verify(transactionConverter).convertToDbDetached(transactions);
        verify(blockchainPublisherService).storeTransactionForDispatchLater(eq(organisationId), transactionsCaptor.capture());

        Set<TransactionEntity> capturedTransactions = transactionsCaptor.getValue();
        assertThat(capturedTransactions).isEqualTo(convertedTransactions);
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
                .accountingPeriod(YearMonth.now())
                .build();
    }

}