package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationRejectionCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterStatusRequest;

import java.util.List;
import java.util.Set;

public interface CustomTransactionRepository {

    List<TransactionEntity> findAllByStatus(String organisationId,
                                            List<TxValidationStatus> txValidationStatuses,
                                            List<TransactionType> transactionType);

    List<Object[]> findAllReconciliationSpecial(Set<ReconcilationRejectionCode> rejectionCodes,
                                                Integer limit,
                                                Integer page);

    List<Object[]> findAllReconciliationSpecialCount(Set<ReconcilationRejectionCode> rejectionCodes,
                                                     Integer limit,
                                                     Integer page);

    List<TransactionEntity> findAllReconciliation(ReconciliationFilterStatusRequest filter,
                                                  Integer limit,
                                                  Integer page);

    Object[] findCalcReconciliationStatistic();

}
