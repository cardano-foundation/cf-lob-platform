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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
        List<ReportEntity> reportEntities = reportRepository.getNewestReportsInRange(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));
        Map<IncomeStatemenCategories, Integer> totalExpenses = new EnumMap<>(IncomeStatemenCategories.class);

        reportEntities.forEach(reportEntity -> {
            if(reportEntity.getIncomeStatementReportData().isPresent()) {
                IncomeStatementData incomeStatementData = reportEntity.getIncomeStatementReportData().get();
                incomeStatementData.getCostOfGoodsAndServices().ifPresent(costOfGoodsAndServices -> totalExpenses.merge(IncomeStatemenCategories.COST_OF_SERVICE, costOfGoodsAndServices.getCostOfProvidingServices().orElse(BigDecimal.ZERO).intValue(), Integer::sum));
                incomeStatementData.getOperatingExpenses().ifPresent(operatingExpenses -> {
                    totalExpenses.merge(IncomeStatemenCategories.PERSONNEL_EXPENSES, operatingExpenses.getPersonnelExpenses().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                    // Other Operating Expenses
                    int otherOperatingExpenses = sumUpOptionalFields(
                            operatingExpenses.getRentExpenses(),
                            operatingExpenses.getGeneralAndAdministrativeExpenses(),
                            operatingExpenses.getAmortizationOnIntangibleAssets(),
                            operatingExpenses.getDepreciationAndImpairmentLossesOnTangibleAssets(),
                            operatingExpenses.getRentExpenses());
                    totalExpenses.merge(IncomeStatemenCategories.OTHER_OPERATING_EXPENSES, otherOperatingExpenses, Integer::sum);
                });
                incomeStatementData.getFinancialIncome().ifPresent(financialIncome -> totalExpenses.merge(IncomeStatemenCategories.FINANCIAL_EXPENSES, financialIncome.getFinancialExpenses().orElse(BigDecimal.ZERO).intValue(), Integer::sum));
                incomeStatementData.getTaxExpenses().ifPresent(taxExpenses -> totalExpenses.merge(IncomeStatemenCategories.TAX_EXPENSES, taxExpenses.getIncomeTaxExpense().orElse(BigDecimal.ZERO).intValue(), Integer::sum));
            }
        });
        return totalExpenses;
    }

    private Map<IncomeStatemenCategories, Integer> getIncomeStream(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getNewestReportsInRange(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));
        Map<IncomeStatemenCategories, Integer> incomeStream = new EnumMap<>(IncomeStatemenCategories.class);


        reportEntities.forEach(reportEntity ->
                reportEntity.getIncomeStatementReportData().ifPresent(incomeStatementData -> {
            incomeStatementData.getFinancialIncome().ifPresent(financialIncome -> {
                incomeStream.merge(IncomeStatemenCategories.STAKING_REWARDS, financialIncome.getStakingRewardsIncome().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                incomeStream.merge(IncomeStatemenCategories.OTHER, financialIncome.getNetIncomeOptionsSale().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                incomeStream.merge(IncomeStatemenCategories.FINANCIAL_INCOME, financialIncome.getFinancialRevenues().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                incomeStream.merge(IncomeStatemenCategories.GAINS_ON_SALES_OF_CRYPTO_CURRENCIES, financialIncome.getRealisedGainsOnSaleOfCryptocurrencies().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
            });
            incomeStatementData.getRevenues().ifPresent(revenues -> {
                incomeStream.merge(IncomeStatemenCategories.BUILDING_OF_PROVISIONS, revenues.getBuildOfLongTermProvision().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                incomeStream.merge(IncomeStatemenCategories.OTHER, revenues.getOtherIncome().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
            });
        }));

        return incomeStream;
    }

    @SafeVarargs
    private int sumUpOptionalFields(Optional<BigDecimal>... fields) {
        return Stream.of(fields)
                .map(field -> field.orElse(BigDecimal.ZERO))
                .map(BigDecimal::intValue)
                .reduce(Integer::sum)
                .orElse(0);
    }

}
