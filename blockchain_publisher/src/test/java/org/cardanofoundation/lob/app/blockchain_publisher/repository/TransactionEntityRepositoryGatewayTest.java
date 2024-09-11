package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Limit;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionEntityRepositoryGatewayTest {

    @Mock
    private TransactionEntityRepository transactionEntityRepository;

    @Mock
    private TransactionItemEntityRepository transactionItemEntityRepository;

    @InjectMocks
    private TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_shouldReturnTransactionEntity() {
        String txId = "tx123";
        TransactionEntity expectedTransaction = new TransactionEntity();
        when(transactionEntityRepository.findById(txId)).thenReturn(Optional.of(expectedTransaction));

        Optional<TransactionEntity> actualTransaction = transactionEntityRepositoryGateway.findById(txId);

        assertEquals(Optional.of(expectedTransaction), actualTransaction);
    }

    @Test
    void findTransactionsByStatus_shouldReturnTransactions() {
        String organisationId = "org1";
        int batchSize = 10;
        Set<TransactionEntity> expectedTransactions = Set.of(new TransactionEntity());
        when(transactionEntityRepository.findTransactionsByStatus(anyString(), any(), any())).thenReturn(expectedTransactions);

        Set<TransactionEntity> actualTransactions = transactionEntityRepositoryGateway.findTransactionsByStatus(organisationId, batchSize);

        assertEquals(expectedTransactions, actualTransactions);
    }

    @Test
    void findDispatchedTransactionsThatAreNotFinalizedYet_shouldReturnFilteredTransactions() {
        String organisationId = "org1";
        long currentTip = 1000L;
        TransactionEntity transaction = new TransactionEntity();

        transaction.setL1SubmissionData(Optional.of(L1SubmissionData.builder()
                .absoluteSlot(500L)
                .build()));

        Set<TransactionEntity> transactions = Set.of(transaction);
        when(transactionEntityRepository.findDispatchedTransactionsThatAreNotFinalizedYet(anyString(), any(), any())).thenReturn(transactions);

        Set<TransactionEntity> actualTransactions = transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(organisationId, Limit.unlimited());

        assertEquals(Set.of(transaction), actualTransactions);
    }

    @Test
    void storeTransaction_shouldSaveTransactionEntity() {
        TransactionEntity transactionEntity = new TransactionEntity();

        transactionEntityRepositoryGateway.storeTransaction(transactionEntity);

        verify(transactionEntityRepository).save(transactionEntity);
    }

}