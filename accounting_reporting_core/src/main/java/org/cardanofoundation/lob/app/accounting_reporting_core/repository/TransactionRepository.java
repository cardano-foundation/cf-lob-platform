package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String>, CustomTransactionRepository {

    @Query("SELECT t FROM accounting_reporting_core.TransactionEntity t" +
            " WHERE t.organisation.id = :organisationId" +
            " AND t.overallStatus = 'OK'" +
            " AND t.ledgerDispatchStatus = 'NOT_DISPATCHED'" +
            " ORDER BY t.createdAt ASC, t.id ASC")
    Set<TransactionEntity> findDispatchableTransactions(@Param("organisationId") String organisationId, Limit limit);

}
