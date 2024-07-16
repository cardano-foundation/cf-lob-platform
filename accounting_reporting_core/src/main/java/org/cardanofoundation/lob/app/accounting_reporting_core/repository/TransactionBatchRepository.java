package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionBatchRepository extends JpaRepository<TransactionBatchEntity, String>, CustomTransactionBatchRepository {
    List<TransactionBatchEntity> findAllByFilteringParametersOrganisationId(String organisationId);
}
