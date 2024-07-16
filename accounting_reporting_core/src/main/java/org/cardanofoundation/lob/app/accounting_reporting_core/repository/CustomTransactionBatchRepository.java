package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;

import java.util.List;

public interface CustomTransactionBatchRepository {
    List<TransactionBatchEntity> findByFilter(BatchSearchRequest body);
}
