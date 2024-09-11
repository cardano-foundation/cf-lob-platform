package org.cardanofoundation.lob.app.blockchain_publisher.service;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BlockchainPublisherServiceTest {

    @Mock
    private TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;

    @Mock
    private LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Mock
    private TransactionConverter transactionConverter;

    @InjectMocks
    private BlockchainPublisherService blockchainPublisherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void storeTransactionsForDispatchLater_shouldStoreNewTransactionsAndPublishEvents() {
        // Arrange
        String organisationId = "org123";
        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction1 = mock(Transaction.class);
        Transaction transaction2 = mock(Transaction.class);
        transactions.add(transaction1);
        transactions.add(transaction2);

        TransactionEntity entity1 = mock(TransactionEntity.class);
        TransactionEntity entity2 = mock(TransactionEntity.class);

        when(transactionConverter.convertToDbDetached(transaction1)).thenReturn(entity1);
        when(transactionConverter.convertToDbDetached(transaction2)).thenReturn(entity2);

        when(transactionEntityRepositoryGateway.storeOnlyNewTransaction(entity1)).thenReturn(Optional.of(entity1));
        when(transactionEntityRepositoryGateway.storeOnlyNewTransaction(entity2)).thenReturn(Optional.empty()); // Assuming one transaction is not new

        // Act
        blockchainPublisherService.storeTransactionsForDispatchLater(organisationId, transactions);

        // Assert
        verify(transactionEntityRepositoryGateway, times(1)).storeOnlyNewTransaction(entity1);
        verify(transactionEntityRepositoryGateway, times(1)).storeOnlyNewTransaction(entity2);

        ArgumentCaptor<Set<TransactionEntity>> captor = ArgumentCaptor.forClass(Set.class);
        verify(ledgerUpdatedEventPublisher).sendLedgerUpdatedEvents(eq(organisationId), captor.capture());

        Set<TransactionEntity> capturedTransactions = captor.getValue();
        assertThat(capturedTransactions).containsExactly(entity1); // Only entity1 should be stored and dispatched
    }

}
