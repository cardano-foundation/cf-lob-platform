package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;

public interface TransactionBatchRepository extends JpaRepository<TransactionBatchEntity, String>, CustomTransactionBatchRepository {

    List<TransactionBatchEntity> findAllByFilteringParametersOrganisationId(String organisationId);

}
