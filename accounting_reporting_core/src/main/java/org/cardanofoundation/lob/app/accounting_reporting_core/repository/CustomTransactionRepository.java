package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterStatusRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationRejectionCodeRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CustomTransactionRepository {

    List<TransactionEntity> findAllByStatus(String organisationId,
                                            List<TxValidationStatus> txValidationStatuses,
                                            List<TransactionType> transactionType);

    List<Object[]> findAllReconciliationSpecial(Set<ReconciliationRejectionCodeRequest> rejectionCodes,
                                                Optional<LocalDate> getDateFrom,
                                                Integer limit,
                                                Integer page);

    List<Object[]> findAllReconciliationSpecialCount(Set<ReconciliationRejectionCodeRequest> rejectionCodes,
                                                     Optional<LocalDate> getDateFrom,
                                                     Integer limit,
                                                     Integer page);

    List<TransactionEntity> findAllReconciliation(ReconciliationFilterStatusRequest filter,
                                                  Integer limit,
                                                  Integer page);

    List<TransactionEntity> findAllReconciliationCount(ReconciliationFilterStatusRequest filter,
                                                       Integer limit,
                                                       Integer page);

    Object findCalcReconciliationStatistic();

}
