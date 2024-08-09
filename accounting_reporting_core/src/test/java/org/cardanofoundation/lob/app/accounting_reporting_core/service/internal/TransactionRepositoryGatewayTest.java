package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Rejection;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.RejectionCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionItemsRejectionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionsRequest;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.RejectionCode.INCORRECT_AMOUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionRepositoryGatewayTest {

    @Mock
    private TransactionItemRepository transactionItemRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private TransactionRepositoryGateway transactionRepositoryGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void approveTransaction_shouldReturnNotFound_whenTransactionDoesNotExist() {
        // Arrange
        String transactionId = "nonexistent_tx_id";
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act
        Either<IdentifiableProblem, TransactionEntity> result = transactionRepositoryGateway.approveTransaction(transactionId);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("TX_NOT_FOUND");
    }

    @Test
    void approveTransaction_shouldReturnFailure_whenTransactionIsFailed() {
        // Arrange
        String transactionId = "failed_tx_id";
        TransactionEntity failedTransaction = new TransactionEntity();
        failedTransaction.setStatus(TransactionStatus.FAIL);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(failedTransaction));

        // Act
        Either<IdentifiableProblem, TransactionEntity> result = transactionRepositoryGateway.approveTransaction(transactionId);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("CANNOT_APPROVE_FAILED_TX");
    }

    @Test
    void approveTransaction_shouldApproveTransaction_whenTransactionIsValid() {
        // Arrange
        String transactionId = "valid_tx_id";
        TransactionEntity validTransaction = new TransactionEntity();
        validTransaction.setStatus(OK); // Assuming SUCCESS is a valid status for approval
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(validTransaction));
        when(transactionRepository.save(validTransaction)).thenReturn(validTransaction);

        // Act
        Either<IdentifiableProblem, TransactionEntity> result = transactionRepositoryGateway.approveTransaction(transactionId);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isSameAs(validTransaction);
        verify(transactionRepository, times(1)).save(validTransaction);
    }

    @Test
    void approveTransactions_shouldApproveValidTransactionsAndHandleErrors() {
        // Arrange
        String validTransactionId = "valid_tx_id";
        String failedTransactionId = "failed_tx_id";

        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(validTransactionId, failedTransactionId));

        TransactionEntity validTransaction = new TransactionEntity();
        validTransaction.setStatus(OK);
        when(transactionRepository.findById(validTransactionId)).thenReturn(Optional.of(validTransaction));
        when(transactionRepository.findById(failedTransactionId)).thenReturn(Optional.empty()); // Simulating not found

        when(transactionRepository.save(validTransaction)).thenReturn(validTransaction);

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactions(transactionsRequest);

        // Assert
        assertThat(results).hasSize(2);

        Either<IdentifiableProblem, TransactionEntity> validResult = results.stream().filter(Either::isRight).findFirst().orElseThrow();
        Either<IdentifiableProblem, TransactionEntity> failedResult = results.stream().filter(Either::isLeft).findFirst().orElseThrow();

        assertThat(validResult.isRight()).isTrue();
        assertThat(failedResult.isLeft()).isTrue();
        assertThat(failedResult.getLeft().getProblem().getTitle()).isEqualTo("TX_NOT_FOUND");

        verify(transactionRepository, times(1)).save(validTransaction);
    }

    @Test
    void approveTransactionsDispatch_shouldApproveDispatchForValidTransactions() {
        // Arrange
        String transactionId = "valid_tx_id";
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(transactionId));

        TransactionEntity validTransaction = new TransactionEntity();
        validTransaction.setStatus(OK);
        validTransaction.setTransactionApproved(true);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(validTransaction));
        when(transactionRepository.save(validTransaction)).thenReturn(validTransaction);

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest);

        // Assert
        assertThat(results).hasSize(1);
        Either<IdentifiableProblem, TransactionEntity> result = results.get(0);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isSameAs(validTransaction);

        verify(transactionRepository, times(1)).save(validTransaction);
        verify(ledgerService, times(1)).checkIfThereAreTransactionsToDispatch(any(), any());
    }

    @Test
    void rejectTransactionItems_shouldRejectItemsProperly() {
        // Arrange
        String transactionId = "tx_id";
        String transactionItemId = "tx_item_id";

        TransactionItemsRejectionRequest rejectionRequest = new TransactionItemsRejectionRequest();
        rejectionRequest.setTransactionId(transactionId);
        rejectionRequest.setTransactionItemsRejections(Set.of(new TransactionItemsRejectionRequest.TxItemRejectionRequest(transactionItemId, INCORRECT_AMOUNT)));

        TransactionEntity transactionEntity = new TransactionEntity();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionEntity));

        TransactionItemEntity transactionItemEntity = new TransactionItemEntity();
        when(transactionItemRepository.findById(transactionItemId)).thenReturn(Optional.of(transactionItemEntity));

        when(transactionItemRepository.save(transactionItemEntity)).thenReturn(transactionItemEntity);

        // Act
        List<Either<IdentifiableProblem, TransactionItemEntity>> results = transactionRepositoryGateway.rejectTransactionItems(rejectionRequest);

        // Assert
        assertThat(results).isNotEmpty();

        Either<IdentifiableProblem, TransactionItemEntity> result = results.get(0);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getRejection().orElseThrow().getRejectionCode()).isEqualTo(INCORRECT_AMOUNT);

        verify(transactionItemRepository, times(1)).save(transactionItemEntity);
    }

    @Test
    void findById_shouldReturnTransaction_whenExists() {
        // Arrange
        String transactionId = "tx_id";
        TransactionEntity transactionEntity = new TransactionEntity();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionEntity));

        // Act
        Optional<TransactionEntity> result = transactionRepositoryGateway.findById(transactionId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(transactionEntity);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        // Arrange
        String transactionId = "tx_id";
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act
        Optional<TransactionEntity> result = transactionRepositoryGateway.findById(transactionId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void approveTransaction_shouldReturnRejectionResponse_whenTransactionHasRejection() {
        // Arrange
        String transactionId = "rejected_tx_id";

        TransactionEntity rejectedTransaction = new TransactionEntity();
        rejectedTransaction.setStatus(OK);
        rejectedTransaction.setTransactionApproved(false);

        TransactionItemEntity transactionItemEntity = new TransactionItemEntity();
        transactionItemEntity.setId("rejected_item_id");
        transactionItemEntity.setRejection(Optional.of(new Rejection(INCORRECT_AMOUNT)));
        transactionItemEntity.setTransaction(rejectedTransaction);

        rejectedTransaction.setItems(Set.of(transactionItemEntity));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(rejectedTransaction));

        Either<IdentifiableProblem, TransactionEntity> result = transactionRepositoryGateway.approveTransaction(transactionId);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("CANNOT_APPROVE_REJECTED_TX");
    }

    @Test
    void approveTransactionsDispatch_shouldReturnError_whenDispatchingUnapprovedTransaction() {
        // Arrange
        String transactionId = "unapproved_tx_id";
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(transactionId));

        TransactionEntity unapprovedTransaction = new TransactionEntity();
        unapprovedTransaction.setStatus(OK);
        unapprovedTransaction.setTransactionApproved(false); // Not approved
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(unapprovedTransaction));

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest);

        // Assert
        assertThat(results).hasSize(1);
        Either<IdentifiableProblem, TransactionEntity> result = results.get(0);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("TX_NOT_APPROVED");
    }

    @Test
    void rejectTransactionItems_shouldReturnError_whenTransactionItemNotFound() {
        // Arrange
        String transactionId = "tx_id";
        String transactionItemId = "nonexistent_item_id";

        TransactionItemsRejectionRequest rejectionRequest = new TransactionItemsRejectionRequest();
        rejectionRequest.setTransactionId(transactionId);
        rejectionRequest.setTransactionItemsRejections(Set.of(new TransactionItemsRejectionRequest.TxItemRejectionRequest(transactionItemId, INCORRECT_AMOUNT)));

        TransactionEntity transactionEntity = new TransactionEntity();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionEntity));

        when(transactionItemRepository.findById(transactionItemId)).thenReturn(Optional.empty());

        // Act
        List<Either<IdentifiableProblem, TransactionItemEntity>> results = transactionRepositoryGateway.rejectTransactionItems(rejectionRequest);

        // Assert
        assertThat(results).isNotEmpty();
        Either<IdentifiableProblem, TransactionItemEntity> result = results.get(0);
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("TX_ITEM_NOT_FOUND");

        verify(transactionItemRepository, never()).save(any(TransactionItemEntity.class));
    }

    @Test
    void rejectTransactionItems_shouldReturnError_whenTransactionAlreadyApprovedForDispatch() {
        // Arrange
        String transactionId = "tx_id";
        String transactionItemId = "tx_item_id";

        TransactionItemsRejectionRequest rejectionRequest = new TransactionItemsRejectionRequest();
        rejectionRequest.setTransactionId(transactionId);
        rejectionRequest.setTransactionItemsRejections(Set.of(new TransactionItemsRejectionRequest.TxItemRejectionRequest(transactionItemId, INCORRECT_AMOUNT)));

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setLedgerDispatchApproved(true); // Transaction already approved for dispatch
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transactionEntity));

        TransactionItemEntity transactionItemEntity = new TransactionItemEntity();
        when(transactionItemRepository.findById(transactionItemId)).thenReturn(Optional.of(transactionItemEntity));

        // Act
        List<Either<IdentifiableProblem, TransactionItemEntity>> results = transactionRepositoryGateway.rejectTransactionItems(rejectionRequest);

        // Assert
        assertThat(results).isNotEmpty();
        Either<IdentifiableProblem, TransactionItemEntity> result = results.get(0);
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("TX_ALREADY_APPROVED_CANNOT_REJECT_TX_ITEM");

        verify(transactionItemRepository, never()).save(any(TransactionItemEntity.class));
    }

    @Test
    void approveTransactions_shouldHandleDataAccessExceptionDuringApproval() {
        // Arrange
        String transactionId = "tx_id";
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(transactionId));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(new TransactionEntity()));
        when(transactionRepository.save(any(TransactionEntity.class))).thenThrow(new DataAccessException("Database error") {});

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactions(transactionsRequest);

        // Assert
        assertThat(results).hasSize(1);
        Either<IdentifiableProblem, TransactionEntity> result = results.get(0);
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("DB_ERROR");

        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
    }

    @Test
    void approveTransactionsDispatch_shouldHandleDataAccessExceptionDuringDispatchApproval() {
        // Arrange
        String transactionId = "tx_id";
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(transactionId));

        TransactionEntity validTransaction = new TransactionEntity();
        validTransaction.setStatus(OK);
        validTransaction.setTransactionApproved(true);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(validTransaction));
        when(transactionRepository.save(validTransaction)).thenThrow(new DataAccessException("Database error") {});

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest);

        // Assert
        assertThat(results).hasSize(1);
        Either<IdentifiableProblem, TransactionEntity> result = results.get(0);
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("DB_ERROR");

        verify(transactionRepository, times(1)).save(validTransaction);
    }


    // start

    @Test
    void approveTransaction_shouldReturnRejectionResponse_whenAnyTransactionItemHasRejection() {
        // Arrange
        String transactionId = "rejected_tx_id";
        TransactionEntity transaction = new TransactionEntity();
        transaction.setStatus(OK);

        TransactionItemEntity itemWithRejection = new TransactionItemEntity();
        itemWithRejection.setId("rejected_item_id1");
        itemWithRejection.setRejection(Optional.of(new Rejection(INCORRECT_AMOUNT)));
        itemWithRejection.setTransaction(transaction);

        TransactionItemEntity validItem = new TransactionItemEntity();
        validItem.setId("rejected_item_id2");
        validItem.setRejection(Optional.empty());
        validItem.setTransaction(transaction);

        transaction.setItems(Set.of(itemWithRejection, validItem));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        Either<IdentifiableProblem, TransactionEntity> result = transactionRepositoryGateway.approveTransaction(transactionId);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("CANNOT_APPROVE_REJECTED_TX");
    }

    @Test
    void approveTransaction_shouldApproveTransaction_whenAllTransactionItemsAreValid() {
        // Arrange
        String transactionId = "valid_tx_id";
        TransactionEntity transaction = new TransactionEntity();
        transaction.setStatus(OK);

        TransactionItemEntity validItem1 = new TransactionItemEntity();
        validItem1.setId("valid_item_1");
        validItem1.setRejection(Optional.empty());
        validItem1.setTransaction(transaction);

        TransactionItemEntity validItem2 = new TransactionItemEntity();
        validItem2.setId("valid_item_2");
        validItem2.setRejection(Optional.empty());
        validItem2.setTransaction(transaction);

        transaction.setItems(Set.of(validItem1, validItem2));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        Either<IdentifiableProblem, TransactionEntity> result = transactionRepositoryGateway.approveTransaction(transactionId);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isSameAs(transaction);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void approveTransactionsDispatch_shouldReturnError_whenAnyTransactionItemHasRejection() {
        // Arrange
        String transactionId = "tx_id";
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(transactionId));

        TransactionEntity transaction = new TransactionEntity();
        transaction.setStatus(OK);
        transaction.setTransactionApproved(true);

        TransactionItemEntity itemWithRejection = new TransactionItemEntity();
        itemWithRejection.setRejection(Optional.of(new Rejection(RejectionCode.INCORRECT_VAT_CODE)));
        itemWithRejection.setTransaction(transaction);

        transaction.setItems(Set.of(itemWithRejection));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest);

        // Assert
        assertThat(results).hasSize(1);
        Either<IdentifiableProblem, TransactionEntity> result = results.get(0);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getProblem().getTitle()).isEqualTo("CANNOT_APPROVE_REJECTED_TX");
    }

    @Test
    void approveTransactionsDispatch_shouldApproveDispatch_whenAllTransactionItemsAreValid() {
        // Arrange
        String transactionId = "valid_tx_id";
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setTransactionIds(Set.of(transactionId));

        TransactionEntity transaction = new TransactionEntity();
        transaction.setStatus(OK);
        transaction.setTransactionApproved(true);

        TransactionItemEntity validItem = new TransactionItemEntity();
        validItem.setRejection(Optional.empty());
        validItem.setTransaction(transaction);

        transaction.setItems(Set.of(validItem));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        List<Either<IdentifiableProblem, TransactionEntity>> results = transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest);

        // Assert
        assertThat(results).hasSize(1);
        Either<IdentifiableProblem, TransactionEntity> result = results.get(0);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isSameAs(transaction);

        verify(transactionRepository, times(1)).save(transaction);
        verify(ledgerService, times(1)).checkIfThereAreTransactionsToDispatch(any(), any());
    }

}