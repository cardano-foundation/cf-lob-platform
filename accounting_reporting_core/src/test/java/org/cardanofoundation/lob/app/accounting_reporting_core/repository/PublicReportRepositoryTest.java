package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.IntervalType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;

@ExtendWith(MockitoExtension.class)
class PublicReportRepositoryTest {

    @Mock
    private EntityManager em;

    @Test
    void findAllByTypeAndPeriodTest() {
        String query = """
                SELECT r FROM accounting_reporting_core.report.ReportEntity r
                LEFT JOIN accounting_reporting_core.report.ReportEntity r2 on r.idControl = r2.idControl and r.ver < r2.ver
                WHERE r2.idControl IS NULL
                AND r.ledgerDispatchStatus = 'FINALIZED'
                AND r.organisation.id = '75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94'
                AND r.type = 'INCOME_STATEMENT'
                AND r.intervalType = 'MONTH'
                AND r.year = 2025
                AND r.period = 1
                ORDER BY r.createdAt ASC, r.reportId ASC
                """;
        jakarta.persistence.Query queryResult = Mockito.mock(Query.class);
        PublicReportRepository publicReportRepository = new PublicReportRepository(em);
        Mockito.when(em.createQuery(Mockito.anyString())).thenReturn(queryResult);
        publicReportRepository.findAllByTypeAndPeriod("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",ReportType.INCOME_STATEMENT, IntervalType.MONTH, (short) 2025, (short) 1);
        Mockito.verify(em, Mockito.times(1)).createQuery(query);
    }

    @Test
    void findAllByTypeAndPeriodTestNoReportType() {
        String query = """
                SELECT r FROM accounting_reporting_core.report.ReportEntity r
                LEFT JOIN accounting_reporting_core.report.ReportEntity r2 on r.idControl = r2.idControl and r.ver < r2.ver
                WHERE r2.idControl IS NULL
                AND r.ledgerDispatchStatus = 'FINALIZED'
                AND r.organisation.id = '75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94'
                AND r.intervalType = 'MONTH'
                AND r.year = 2025
                AND r.period = 1
                ORDER BY r.createdAt ASC, r.reportId ASC
                """;
        jakarta.persistence.Query queryResult = Mockito.mock(Query.class);
        PublicReportRepository publicReportRepository = new PublicReportRepository(em);
        Mockito.when(em.createQuery(Mockito.anyString())).thenReturn(queryResult);
        publicReportRepository.findAllByTypeAndPeriod("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",null, IntervalType.MONTH, (short) 2025, (short) 1);
        Mockito.verify(em, Mockito.times(1)).createQuery(query);
    }

    @Test
    void findAllByTypeAndPeriodTestNoIntervalType() {
        String query = """
                SELECT r FROM accounting_reporting_core.report.ReportEntity r
                LEFT JOIN accounting_reporting_core.report.ReportEntity r2 on r.idControl = r2.idControl and r.ver < r2.ver
                WHERE r2.idControl IS NULL
                AND r.ledgerDispatchStatus = 'FINALIZED'
                AND r.organisation.id = '75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94'
                AND r.type = 'BALANCE_SHEET'
                ORDER BY r.createdAt ASC, r.reportId ASC
                """;
        jakarta.persistence.Query queryResult = Mockito.mock(Query.class);
        PublicReportRepository publicReportRepository = new PublicReportRepository(em);
        Mockito.when(em.createQuery(Mockito.anyString())).thenReturn(queryResult);
        publicReportRepository.findAllByTypeAndPeriod("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",ReportType.BALANCE_SHEET,null, (short) 2025, (short) 1);
        Mockito.verify(em, Mockito.times(1)).createQuery(query);
    }

    @Test
    void findAllByTypeAndPeriodTestNofilter() {
        String query = """
                SELECT r FROM accounting_reporting_core.report.ReportEntity r
                LEFT JOIN accounting_reporting_core.report.ReportEntity r2 on r.idControl = r2.idControl and r.ver < r2.ver
                WHERE r2.idControl IS NULL
                AND r.ledgerDispatchStatus = 'FINALIZED'
                AND r.organisation.id = '75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94'
                ORDER BY r.createdAt ASC, r.reportId ASC
                """;
        jakarta.persistence.Query queryResult = Mockito.mock(Query.class);
        PublicReportRepository publicReportRepository = new PublicReportRepository(em);
        Mockito.when(em.createQuery(Mockito.anyString())).thenReturn(queryResult);
        publicReportRepository.findAllByTypeAndPeriod("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",null,null, (short) 2025, (short) 1);
        Mockito.verify(em, Mockito.times(1)).createQuery(query);
    }
}
