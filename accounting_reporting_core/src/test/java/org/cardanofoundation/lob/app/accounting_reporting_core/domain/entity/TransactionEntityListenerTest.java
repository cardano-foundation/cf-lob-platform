package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.FAIL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;
import static org.mockito.Mockito.*;

public class TransactionEntityListenerTest {

    private TransactionEntityListener listener;

    private TransactionEntity transactionEntity;

    @BeforeEach
    public void setUp() {
        listener = new TransactionEntityListener();
        transactionEntity = mock(TransactionEntity.class);
    }

    @Test
    public void testCalcStatus_Success() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(ValidationStatus.VALIDATED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(OK);
    }

    @Test
    public void testCalcStatus_Fail_dueToApprovalsNotPassed() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(false);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(ValidationStatus.VALIDATED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(FAIL);
    }

    @Test
    public void testCalcStatus_Fail_dueToRejection() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(false);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(ValidationStatus.VALIDATED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(FAIL);
    }

    @Test
    public void testCalcStatus_Fail_dueToValidationStatusNotValidated() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(ValidationStatus.FAILED);

        TransactionStatus status = listener.calcStatus(transactionEntity);

        assertThat(status).isEqualTo(FAIL);
    }

    @Test
    public void testUpdate_Success() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(true);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(ValidationStatus.VALIDATED);

        listener.update(transactionEntity);

        verify(transactionEntity).setStatus(OK);
    }

    @Test
    public void testUpdate_Fail() {
        when(transactionEntity.allApprovalsPassedForTransactionDispatch()).thenReturn(false);
        when(transactionEntity.isRejectionFree()).thenReturn(true);
        when(transactionEntity.getAutomatedValidationStatus()).thenReturn(ValidationStatus.VALIDATED);

        listener.update(transactionEntity);

        verify(transactionEntity).setStatus(FAIL);
    }

}
