package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.NOK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reconcilation.Reconcilation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reconcilation.ReconcilationCode;

public class OverallStatusTransactionEntityListenerTest {

    private OverallStatusTransactionEntityListener listener;

    private TransactionEntity transactionEntity;

    @BeforeEach
    public void setUp() {
        listener = new OverallStatusTransactionEntityListener();
        transactionEntity = mock(TransactionEntity.class);
    }

    // Existing tests for calcStatus...

    @Test
    public void testCalcStatus_Success() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(OK);
    }

    @Test
    public void testCalcStatus_Fail_dueToApprovalsNotPassed() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(false);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(NOK);
    }

    @Test
    public void testCalcStatus_Fail_dueToRejection() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(false);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(NOK);
    }

    @Test
    public void testCalcStatus_Fail_dueToValidationStatusNotValidated() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.FAILED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(NOK);
    }

    // New tests for reconcilationCode...

    @Test
    public void testReconcilationCode_BothOK() {
        Reconcilation reconcilation = Reconcilation.builder()
                .source(ReconcilationCode.OK)
                .sink(ReconcilationCode.OK)
                .build();

        when(transactionEntity.getReconcilation()).thenReturn(Optional.of(reconcilation));

        Optional<ReconcilationCode> code = listener.reconcilationCode(transactionEntity);

        assertThat(code).isPresent();
        assertThat(code.get()).isEqualTo(ReconcilationCode.OK);
    }

    @Test
    public void testReconcilationCode_SourceNOK() {
        Reconcilation reconcilation = Reconcilation.builder()
                .source(ReconcilationCode.NOK)
                .sink(ReconcilationCode.OK)
                .build();

        when(transactionEntity.getReconcilation()).thenReturn(Optional.of(reconcilation));

        Optional<ReconcilationCode> code = listener.reconcilationCode(transactionEntity);

        assertThat(code).isPresent();
        assertThat(code.get()).isEqualTo(ReconcilationCode.NOK);
    }

    @Test
    public void testReconcilationCode_SinkEmpty() {
        Reconcilation reconcilation = Reconcilation.builder()
                .source(ReconcilationCode.OK)
                .build();

        when(transactionEntity.getReconcilation()).thenReturn(Optional.of(reconcilation));

        Optional<ReconcilationCode> code = listener.reconcilationCode(transactionEntity);

        assertThat(code).isNotPresent();
    }

    @Test
    public void testReconcilationCode_NoReconcilation() {
        when(transactionEntity.getReconcilation()).thenReturn(Optional.empty());

        Optional<ReconcilationCode> code = listener.reconcilationCode(transactionEntity);

        assertThat(code).isNotPresent();
    }

    // Updated tests for update method...

    @Test
    public void testUpdate_Success() {
        // Setup for calcStatus
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        // Setup for reconcilationCode
        Reconcilation reconcilation = Reconcilation.builder()
                .source(ReconcilationCode.OK)
                .sink(ReconcilationCode.OK)
                .build();
        when(transactionEntity.getReconcilation()).thenReturn(Optional.of(reconcilation));

        // Capture the arguments passed to setReconcilation
        ArgumentCaptor<Optional<Reconcilation>> reconcilationCaptor = ArgumentCaptor.forClass(Optional.class);

        listener.update(transactionEntity);

        verify(transactionEntity).setOverallStatus(OK);
        verify(transactionEntity).setReconcilation(reconcilationCaptor.capture());

        Optional<Reconcilation> updatedReconcilationOpt = reconcilationCaptor.getValue();
        assertThat(updatedReconcilationOpt).isPresent();
        Reconcilation updatedReconcilation = updatedReconcilationOpt.get();

        // Verify that finalStatus is set to ReconcilationCode.OK
        assertThat(updatedReconcilation.getFinalStatus()).isEqualTo(Optional.of(ReconcilationCode.OK));
    }

    @Test
    public void testUpdate_Fail() {
        // Setup for calcStatus
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(false);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        // Setup for reconcilationCode
        Reconcilation reconcilation = Reconcilation.builder()
                .source(ReconcilationCode.NOK)
                .sink(ReconcilationCode.OK)
                .build();
        when(transactionEntity.getReconcilation()).thenReturn(Optional.of(reconcilation));

        // Capture the arguments passed to setReconcilation
        ArgumentCaptor<Optional<Reconcilation>> reconcilationCaptor = ArgumentCaptor.forClass(Optional.class);

        listener.update(transactionEntity);

        verify(transactionEntity).setOverallStatus(NOK);
        verify(transactionEntity).setReconcilation(reconcilationCaptor.capture());

        Optional<Reconcilation> updatedReconcilationOpt = reconcilationCaptor.getValue();
        assertThat(updatedReconcilationOpt).isPresent();
        Reconcilation updatedReconcilation = updatedReconcilationOpt.get();

        // Verify that finalStatus is set to ReconcilationCode.NOK
        assertThat(updatedReconcilation.getFinalStatus()).isEqualTo(Optional.of(ReconcilationCode.NOK));
    }

    @Test
    public void testUpdate_NoReconcilation() {
        // Setup for calcStatus
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        // Setup for reconcilationCode
        when(transactionEntity.getReconcilation()).thenReturn(Optional.empty());

        listener.update(transactionEntity);

        verify(transactionEntity).setOverallStatus(OK);
        // Verify that setReconcilation is called with Optional.empty()
        verify(transactionEntity).setReconcilation(Optional.empty());
    }

    @Test
    public void testUpdate_ReconcilationSinkEmpty() {
        // Setup for calcStatus
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(TxValidationStatus.VALIDATED);

        // Setup for reconcilationCode
        Reconcilation reconcilation = Reconcilation.builder()
                .source(ReconcilationCode.OK)
                .build();
        when(transactionEntity.getReconcilation()).thenReturn(Optional.of(reconcilation));

        // Capture the arguments passed to setReconcilation
        ArgumentCaptor<Optional<Reconcilation>> reconcilationCaptor = ArgumentCaptor.forClass(Optional.class);

        listener.update(transactionEntity);

        verify(transactionEntity).setOverallStatus(OK);
        verify(transactionEntity).setReconcilation(reconcilationCaptor.capture());

        Optional<Reconcilation> updatedReconcilationOpt = reconcilationCaptor.getValue();
        assertThat(updatedReconcilationOpt).isPresent();
        Reconcilation updatedReconcilation = updatedReconcilationOpt.get();

        // Verify that finalStatus is not present
        assertThat(updatedReconcilation.getFinalStatus()).isNotPresent();
    }

}
