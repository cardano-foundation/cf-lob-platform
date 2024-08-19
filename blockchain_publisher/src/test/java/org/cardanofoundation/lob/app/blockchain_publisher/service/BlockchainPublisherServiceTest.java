package org.cardanofoundation.lob.app.blockchain_publisher.service;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BlockchainPublisherServiceTest {

    @Mock
    private TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;

    @Mock
    private LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @InjectMocks
    private BlockchainPublisherService blockchainPublisherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void storeTransactionForDispatchLater_shouldStoreNewTransactionsAndPublishEvents() {
        // Arrange
        String organisationId = "org123";
        Set<TransactionEntity> transactions = new HashSet<>();
        TransactionEntity transaction1 = mock(TransactionEntity.class);
        TransactionEntity transaction2 = mock(TransactionEntity.class);
        transactions.add(transaction1);
        transactions.add(transaction2);

        Set<TransactionEntity> storedTransactions = new HashSet<>();
        storedTransactions.add(transaction1); // Assume only transaction1 is new
        when(transactionEntityRepositoryGateway.storeOnlyNewTransactions(transactions))
                .thenReturn(storedTransactions);

        // Act
        blockchainPublisherService.storeTransactionForDispatchLater(organisationId, transactions);

        // Assert
        verify(transactionEntityRepositoryGateway).storeOnlyNewTransactions(transactions);

        ArgumentCaptor<Set<TransactionEntity>> captor = ArgumentCaptor.forClass(Set.class);
        verify(ledgerUpdatedEventPublisher).sendLedgerUpdatedEvents(eq(organisationId), captor.capture());

        Set<TransactionEntity> capturedTransactions = captor.getValue();
        assertThat(capturedTransactions).containsExactlyInAnyOrderElementsOf(storedTransactions);
    }

}