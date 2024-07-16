package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import java.util.List;

public interface CustomTransactionRepository {
    List<TransactionEntity> findAllByStatus(String organisationId,
                                            List<ValidationStatus> validationStatuses,
                                            List<TransactionType> transactionType);
}
