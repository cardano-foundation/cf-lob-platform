package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.IncomeStatementData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricExecutor;

@Component
@RequiredArgsConstructor
public class IncomeStatementMetricService extends MetricExecutor {

    private final ReportRepository reportRepository;

    private static final String COST_OF_SERVICE = "Cost of Service";
    private static final String PERSONNEL_EXPENSES = "Personnel Expenses";
    private static final String FINANCIAL_EXPENSES = "Financial Expenses";
    private static final String TAX_EXPENSES = "Tax Expenses";
    private static final String OTHER_OPERATING_EXPENSES = "Other Operating Expenses";

    @PostConstruct
    public void init() {
        name = MetricEnum.INCOME_STATEMENT;
        metrics = Map.of(
                MetricEnum.SubMetric.TOTAL_EXPENSES, this::getTotalExpenses,
                MetricEnum.SubMetric.INCOME_STREAMS, this::getIncomeStream
        );
    }

    private Map<String, Integer> getTotalExpenses(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {


        List<ReportEntity> reportEntitiesByDateBetween = reportRepository.getReportEntitiesByDateBetween(organisationID,
                startDate.orElse(LocalDate.MIN),
                endDate.orElse(LocalDate.MAX));
        Map<String, Integer> totalExpenses = new HashMap<>();

        reportEntitiesByDateBetween.forEach(reportEntity -> {
            if(reportEntity.getIncomeStatementReportData().isPresent()) {
                IncomeStatementData incomeStatementData = reportEntity.getIncomeStatementReportData().get();
                totalExpenses.merge(COST_OF_SERVICE, incomeStatementData.getCostOfGoodsAndServices()
                        .orElse(new IncomeStatementData.CostOfGoodsAndServices()).getCostOfProvidingServices()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                totalExpenses.merge(PERSONNEL_EXPENSES, incomeStatementData.getOperatingExpenses()
                        .orElse(new IncomeStatementData.OperatingExpenses()).getPersonnelExpenses()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                totalExpenses.merge(FINANCIAL_EXPENSES, incomeStatementData.getFinancialIncome()
                        .orElse(new IncomeStatementData.FinancialIncome()).getFinancialExpenses()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                totalExpenses.merge(TAX_EXPENSES, incomeStatementData.getTaxExpenses()
                        .orElse(new IncomeStatementData.TaxExpenses()).getIncomeTaxExpense()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                // Other Expenses
                totalExpenses.put(OTHER_OPERATING_EXPENSES, 0);
                if(incomeStatementData.getOperatingExpenses().isPresent()) {
                    IncomeStatementData.OperatingExpenses operatingExpenses = incomeStatementData.getOperatingExpenses().get();
                    int otherExpenses = 0;
                    // TODO Check what's in other Expenses as well
                    otherExpenses += operatingExpenses.getRentExpenses().orElse(BigDecimal.ZERO).intValue();
                    otherExpenses += operatingExpenses.getGeneralAndAdministrativeExpenses().orElse(BigDecimal.ZERO).intValue();
                    totalExpenses.merge(OTHER_OPERATING_EXPENSES, otherExpenses, Integer::sum);
                }

            }
        });
        return totalExpenses;
    }

    private Map<String, Integer> getIncomeStream(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        return Map.of(
                "IncomeStreams", 1000
        );
    }

}
