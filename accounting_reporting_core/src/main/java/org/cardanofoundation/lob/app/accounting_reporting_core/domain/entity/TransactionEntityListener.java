package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.NOK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

public class TransactionEntityListener {

    protected TransactionStatus calcStatus(TransactionEntity transactionEntity) {
        if (transactionEntity.allApprovalsPassedForTransactionDispatch()
                && transactionEntity.isRejectionFree()
                && transactionEntity.getAutomatedValidationStatus() == VALIDATED
        ) {
            return OK;
        }

        return NOK;
    }

    @PrePersist
    @PreUpdate
    public void update(TransactionEntity transactionEntity) {
        transactionEntity.setOverallStatus(calcStatus(transactionEntity));
    }

}
