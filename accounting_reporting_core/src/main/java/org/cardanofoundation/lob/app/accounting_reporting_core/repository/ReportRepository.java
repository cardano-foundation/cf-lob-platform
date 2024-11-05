package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ReportRepository extends JpaRepository<ReportEntity, String> {

    @Query("""
            SELECT r FROM accounting_reporting_core.report.ReportEntity r
             WHERE r.organisation.id = :organisationId
             AND r.ledgerDispatchStatus = 'NOT_DISPATCHED'
             AND r.ledgerDispatchApproved = true
             ORDER BY r.createdAt ASC, r.reportId ASC""")
    Set<ReportEntity> findDispatchableTransactions(@Param("organisationId") String organisationId,
                                                   Limit limit);

}
