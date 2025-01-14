package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
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


    @Query("""
            SELECT r FROM accounting_reporting_core.report.ReportEntity r
             WHERE r.organisation.id = :organisationId
             ORDER BY r.createdAt ASC, r.reportId ASC""")
    Set<ReportEntity> findByOrganisationId(@Param("organisationId") String organisationId);

    @Query("""
            SELECT r FROM accounting_reporting_core.report.ReportEntity r
             WHERE r.organisation.id = :organisationId
             AND r.idControl = :idControl""")
    Optional<ReportEntity> findByIdControl(@Param("organisationId") String organisationId, @Param("idControl") String idControl);

    @Query("""
            SELECT r FROM accounting_reporting_core.report.ReportEntity r
             WHERE r.organisation.id = :organisationId
             AND r.idControl = :idControl
             ORDER BY r.ver DESC, r.ledgerDispatchApproved ASC
             LIMIT 1""")
    Optional<ReportEntity> findLatestByIdControl(@Param("organisationId") String organisationId, @Param("idControl") String idControl);
}
