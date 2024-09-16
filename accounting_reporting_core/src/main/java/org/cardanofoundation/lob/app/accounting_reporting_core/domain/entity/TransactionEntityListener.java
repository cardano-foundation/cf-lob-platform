package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.support.collections.Optionals;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.NOK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

public class TransactionEntityListener {

    @PrePersist
    @PreUpdate
    public void update(TransactionEntity transactionEntity) {
        transactionEntity.setOverallStatus(calcStatus(transactionEntity));

        transactionEntity.setReconcilation(transactionEntity.getReconcilation().map(r -> r.toBuilder()
                .finalStatus(reconcilationCode(transactionEntity).orElse(null))
                .build()
        ));
    }

    protected TransactionStatus calcStatus(TransactionEntity transactionEntity) {
        if (transactionEntity.allApprovalsPassedForTransactionDispatch()
                && transactionEntity.isRejectionFree()
                && transactionEntity.getAutomatedValidationStatus() == VALIDATED
        ) {
            return OK;
        }

        return NOK;
    }

    protected Optional<ReconcilationCode> reconcilationCode(TransactionEntity transactionEntity) {
        return transactionEntity.getReconcilation().flatMap(r -> {
            return Optionals.zip(r.getSource(), r.getSink(), (source, sink) -> {
                if (source == ReconcilationCode.OK && sink == ReconcilationCode.OK) {
                    return ReconcilationCode.OK;
                }

                return ReconcilationCode.NOK;
            });
        });
    }

}
