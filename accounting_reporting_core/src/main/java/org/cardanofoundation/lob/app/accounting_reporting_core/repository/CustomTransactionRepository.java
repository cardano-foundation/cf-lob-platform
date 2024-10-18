package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationRejectionCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterStatusRequest;

import java.util.List;
import java.util.Set;

public interface CustomTransactionRepository {
    List<TransactionEntity> findAllByStatus(String organisationId,
                                            List<ValidationStatus> validationStatuses,
                                            List<TransactionType> transactionType);

    public List<Object[]> findAllReconciliationSpecial(ReconciliationFilterRequest body);

    public List<Object[]> findAllReconciliationSpecialCount(ReconciliationFilterRequest body);

    public List<TransactionEntity> findAllReconciliation(ReconciliationFilterRequest body);

    public Object[] findCalcReconciliationStatistic();
}
