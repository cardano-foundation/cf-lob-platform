package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.Report;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service("accounting_reporting_core.ReportConverter")
@Slf4j
@RequiredArgsConstructor
public class ReportConverter {

    private final OrganisationConverter organisationConverter;
    private final BalanceSheetConverter balanceSheetConverter;
    private final IncomeStatementConverter incomeStatementConverter;

    public Set<Report> convertFromDbToCanonicalForm(Set<ReportEntity> reportEntities) {
        return reportEntities.stream()
                .map(this::convertToDbDetached)
                .collect(Collectors.toSet());
    }

    private Report convertToDbDetached(ReportEntity reportEntity) {
        return Report.builder()
                .reportId(reportEntity.getReportId())
                .organisation(organisationConverter.convert(reportEntity.getOrganisation()))
                .type(reportEntity.getType())
                .intervalType(reportEntity.getIntervalType())
                .year(reportEntity.getYear())
                .period(reportEntity.getPeriod())
                .mode(reportEntity.getMode())
                .date(reportEntity.getDate())
                .ledgerDispatchApproved(reportEntity.getLedgerDispatchApproved())
                .ledgerDispatchStatus(reportEntity.getLedgerDispatchStatus())
                .balanceSheetData(balanceSheetConverter.convertBalanceSheet(reportEntity.getBalanceSheetReportData()))
                .incomeStatementData(incomeStatementConverter.convertIncomeStatement(reportEntity.getIncomeStatementReportData()))
                .build();
    }

}
