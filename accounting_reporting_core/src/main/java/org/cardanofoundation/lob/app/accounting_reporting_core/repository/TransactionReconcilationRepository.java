package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.reconcilation.ReconcilationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionReconcilationRepository extends JpaRepository<ReconcilationEntity, String> {
}
