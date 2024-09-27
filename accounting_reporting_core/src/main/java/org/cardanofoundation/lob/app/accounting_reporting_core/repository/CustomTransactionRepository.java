package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterStatusRequest;

import java.util.List;
import java.util.Set;

public interface CustomTransactionRepository {
    List<TransactionEntity> findAllByStatus(String organisationId,
                                            List<ValidationStatus> validationStatuses,
                                            List<TransactionType> transactionType);

    public List<TransactionEntity> findAllReconciliation(ReconciliationFilterStatusRequest filter);

    public Object[] findCalcReconciliationStatistic();
}
