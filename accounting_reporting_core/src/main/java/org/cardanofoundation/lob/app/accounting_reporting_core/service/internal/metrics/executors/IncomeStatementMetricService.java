package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.IncomeStatemenCategories;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.IncomeStatementData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IncomeStatementMetricService extends MetricExecutor {

    private final ReportRepository reportRepository;

    @PostConstruct
    public void init() {
        name = MetricEnum.INCOME_STATEMENT;
        metrics = Map.of(
                MetricEnum.SubMetric.TOTAL_EXPENSES, this::getTotalExpenses,
                MetricEnum.SubMetric.INCOME_STREAMS, this::getIncomeStream
        );
    }

    private Map<IncomeStatemenCategories, Integer> getTotalExpenses(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getReportEntitiesByDateBetween(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));
        Map<IncomeStatemenCategories, Integer> totalExpenses = new HashMap<>();

        reportEntities.forEach(reportEntity -> {
            if(reportEntity.getIncomeStatementReportData().isPresent()) {
                IncomeStatementData incomeStatementData = reportEntity.getIncomeStatementReportData().get();
                totalExpenses.merge(IncomeStatemenCategories.COST_OF_SERVICE, incomeStatementData.getCostOfGoodsAndServices()
                        .orElse(new IncomeStatementData.CostOfGoodsAndServices()).getCostOfProvidingServices()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                totalExpenses.merge(IncomeStatemenCategories.PERSONNEL_EXPENSES, incomeStatementData.getOperatingExpenses()
                        .orElse(new IncomeStatementData.OperatingExpenses()).getPersonnelExpenses()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                totalExpenses.merge(IncomeStatemenCategories.FINANCIAL_EXPENSES, incomeStatementData.getFinancialIncome()
                        .orElse(new IncomeStatementData.FinancialIncome()).getFinancialExpenses()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                totalExpenses.merge(IncomeStatemenCategories.TAX_EXPENSES, incomeStatementData.getTaxExpenses()
                        .orElse(new IncomeStatementData.TaxExpenses()).getIncomeTaxExpense()
                        .orElse(BigDecimal.ZERO).intValue(),
                        Integer::sum);
                // Other Expenses
                totalExpenses.put(IncomeStatemenCategories.OTHER_OPERATING_EXPENSES, 0);
                if(incomeStatementData.getOperatingExpenses().isPresent()) {
                    IncomeStatementData.OperatingExpenses operatingExpenses = incomeStatementData.getOperatingExpenses().get();
                    int otherExpenses = 0;
                    // TODO Check what's in other Expenses as well
                    otherExpenses += operatingExpenses.getRentExpenses().orElse(BigDecimal.ZERO).intValue();
                    otherExpenses += operatingExpenses.getGeneralAndAdministrativeExpenses().orElse(BigDecimal.ZERO).intValue();
                    totalExpenses.merge(IncomeStatemenCategories.OTHER_OPERATING_EXPENSES, otherExpenses, Integer::sum);
                }

            }
        });
        return totalExpenses;
    }

    private Map<IncomeStatemenCategories, Integer> getIncomeStream(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getReportEntitiesByDateBetween(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));
        Map<IncomeStatemenCategories, Integer> incomeStream = new HashMap<>();


        reportEntities.forEach(reportEntity -> {
            reportEntity.getIncomeStatementReportData().ifPresent(incomeStatementData -> {
                incomeStatementData.getFinancialIncome().ifPresent(financialIncome -> {
                    incomeStream.merge(IncomeStatemenCategories.STAKING_REWARDS, financialIncome.getStakingRewardsIncome().orElse(BigDecimal.ZERO).intValue(), Integer::sum);

                });
                incomeStatementData.getRevenues().ifPresent(revenues -> {
                    incomeStream.merge(IncomeStatemenCategories.BUILDING_OF_PROVISIONS, revenues.getBuildOfLongTermProvision().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                    incomeStream.merge(IncomeStatemenCategories.OTHER, revenues.getOtherIncome().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                });
            });
        });

        return incomeStream;
    }

}
