package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.reconcilation.ReconcilationEntity;

public interface TransactionReconcilationRepository extends JpaRepository<ReconcilationEntity, String> {
}
