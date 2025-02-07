package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.IntervalType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;

@Slf4j
@RequiredArgsConstructor
@Service
public class PublicReportRepository {
    private final EntityManager em;


    public Set<ReportEntity> findAllByTypeAndPeriod(ReportType reportType, IntervalType intervalType, short year, short period) {
        String query = STR."""
                SELECT r FROM accounting_reporting_core.report.ReportEntity r
                LEFT JOIN accounting_reporting_core.report.ReportEntity r2 on r.idControl = r2.idControl and r.ver < r2.ver
                """;

        String where = STR."""
                WHERE r2.idControl IS NULL
                """;

        if (null != reportType) {
            where += STR."""
             AND r.type = '\{reportType}'
             """;
        }
        if (null != intervalType) {
            where += STR."""
             AND r.intervalType = '\{intervalType}'
             AND r.year = \{year}
             AND r.period = \{period}
             """;
        }

        where += STR."""

                ORDER BY r.createdAt ASC, r.reportId ASC
                """;


        jakarta.persistence.Query resultQuery = em.createQuery(query + where);

        return new HashSet<ReportEntity>(resultQuery.getResultList());
    }
}
