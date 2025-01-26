package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum.BALANCE_SHEET;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.BalanceSheetCategories;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.BalanceSheetData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricExecutor;

@Component
@RequiredArgsConstructor
public class BalanceSheetMetricService extends MetricExecutor {

    private final ReportRepository reportRepository;

    @PostConstruct
    public void init() {
        name = BALANCE_SHEET;
        metrics = Map.of(
                MetricEnum.SubMetric.ASSET_CATEGORIES, this::getAssetCategories,
                MetricEnum.SubMetric.BALANCE_SHEET_OVERVIEW, this::getBalanceSheetOverview
        );
    }

    private Map<BalanceSheetCategories, Integer> getAssetCategories(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getNewestReportsInRange(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));

        Map<BalanceSheetCategories, Integer> assetCategories = new EnumMap<>(BalanceSheetCategories.class);

        reportEntities.forEach(reportEntity ->
                reportEntity.getBalanceSheetReportData().flatMap(BalanceSheetData::getAssets).ifPresent(assets -> {
            assets.getCurrentAssets().ifPresent(currentAssets -> {
                currentAssets.getCashAndCashEquivalents().ifPresent(
                        cash -> assetCategories.merge(BalanceSheetCategories.CASH, cash.intValue(), Integer::sum));
                currentAssets.getCryptoAssets().ifPresent(
                        cryptoAssets -> assetCategories.merge(BalanceSheetCategories.CRYPTO_ASSETS, cryptoAssets.intValue(), Integer::sum));
                currentAssets.getOtherReceivables().ifPresent(
                        otherReceivables -> assetCategories.merge(BalanceSheetCategories.OTHER, otherReceivables.intValue(), Integer::sum));
                currentAssets.getPrepaymentsAndOtherShortTermAssets().ifPresent(
                        prepayments -> assetCategories.merge(BalanceSheetCategories.OTHER, prepayments.intValue(), Integer::sum));
            });

            assets.getNonCurrentAssets().ifPresent(nonCurrentAssets -> {
                nonCurrentAssets.getFinancialAssets().ifPresent(
                        financialAssets -> assetCategories.merge(BalanceSheetCategories.FINANCIAL_ASSETS, financialAssets.intValue(), Integer::sum));
                nonCurrentAssets.getIntangibleAssets().ifPresent(
                        intangibleAssets -> assetCategories.merge(BalanceSheetCategories.OTHER, intangibleAssets.intValue(), Integer::sum));
                nonCurrentAssets.getInvestments().ifPresent(
                        investments -> assetCategories.merge(BalanceSheetCategories.OTHER, investments.intValue(), Integer::sum));
                nonCurrentAssets.getPropertyPlantEquipment().ifPresent(
                        propertyPlantEquipment -> assetCategories.merge(BalanceSheetCategories.OTHER, propertyPlantEquipment.intValue(), Integer::sum));
            });
        }));

        return assetCategories;
    }

    private Map<BalanceSheetCategories, Map<BalanceSheetCategories, Integer>> getBalanceSheetOverview(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getNewestReportsInRange(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));

        Map<BalanceSheetCategories, Map<BalanceSheetCategories, Integer>> balanceSheetOverview = new EnumMap<>(BalanceSheetCategories.class);

        reportEntities.forEach(reportEntity -> reportEntity.getBalanceSheetReportData().ifPresent(balanceSheetData -> {
            balanceSheetData.getAssets().ifPresent(assets -> {
                Map<BalanceSheetCategories, Integer> assetMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.ASSETS, new HashMap<>());
                processAssets(assets, assetMap);
                balanceSheetOverview.put(BalanceSheetCategories.ASSETS, assetMap);
            });

            balanceSheetData.getLiabilities().ifPresent(liabilities -> {
                Map<BalanceSheetCategories, Integer> liabilityMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.LIABILITIES, new HashMap<>());
                processLiabilities(liabilities, liabilityMap);
                balanceSheetOverview.put(BalanceSheetCategories.LIABILITIES, liabilityMap);
            });
            balanceSheetData.getCapital().ifPresent(capital -> {
                Map<BalanceSheetCategories, Integer> liabilityMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.LIABILITIES, new HashMap<>());
                processCapital(capital, liabilityMap);
                balanceSheetOverview.put(BalanceSheetCategories.LIABILITIES, liabilityMap);
            });
        }));
        return balanceSheetOverview;
    }

    private static void processCapital(BalanceSheetData.Capital capital, Map<BalanceSheetCategories, Integer> liabilityMap) {
        capital.getCapital().ifPresent(
                capitalValue -> liabilityMap.merge(BalanceSheetCategories.CAPITAL, capitalValue.intValue(), Integer::sum));
        capital.getProfitForTheYear().ifPresent(
                profitForTheYear -> liabilityMap.merge(BalanceSheetCategories.PROFIT_OF_THE_YEAR, profitForTheYear.intValue(), Integer::sum));
        capital.getResultsCarriedForward().ifPresent(
                resultsCarriedForward -> liabilityMap.merge(BalanceSheetCategories.RESULTS_CARRIED_FORWARD, resultsCarriedForward.intValue(), Integer::sum));
    }

    private static void processLiabilities(BalanceSheetData.Liabilities liabilities, Map<BalanceSheetCategories, Integer> liabilityMap) {
        liabilities.getCurrentLiabilities().ifPresent(currentLiabilities -> {
            currentLiabilities.getAccrualsAndShortTermProvisions().ifPresent(
                    accruals -> liabilityMap.merge(BalanceSheetCategories.ACCRUSAL_AND_SHORT_TERM_PROVISIONS, accruals.intValue(), Integer::sum));
            currentLiabilities.getTradeAccountsPayables().ifPresent(
                    tradeAccountsPayables -> liabilityMap.merge(BalanceSheetCategories.TRADE_ACCOUNTS_PAYABLE, tradeAccountsPayables.intValue(), Integer::sum));
            currentLiabilities.getOtherCurrentLiabilities().ifPresent(
                    otherCurrentLiabilities -> liabilityMap.merge(BalanceSheetCategories.OTHER, otherCurrentLiabilities.intValue(), Integer::sum));
        });
        liabilities.getNonCurrentLiabilities().flatMap(BalanceSheetData.Liabilities.NonCurrentLiabilities::getProvisions).ifPresent(
                provisions -> liabilityMap.merge(BalanceSheetCategories.PROVISIONS, provisions.intValue(), Integer::sum));
    }

    private static void processAssets(BalanceSheetData.Assets assets, Map<BalanceSheetCategories, Integer> assetMap) {
        assets.getCurrentAssets().ifPresent(currentAssets -> {
            currentAssets.getCryptoAssets().ifPresent(
                    cryptoAssets -> assetMap.merge(BalanceSheetCategories.CRYPTO_ASSETS, cryptoAssets.intValue(), Integer::sum));
            currentAssets.getCashAndCashEquivalents().ifPresent(
                    cash -> assetMap.merge(BalanceSheetCategories.CASH, cash.intValue(), Integer::sum));
            currentAssets.getOtherReceivables().ifPresent(
                    otherReceivables -> assetMap.merge(BalanceSheetCategories.OTHER, otherReceivables.intValue(), Integer::sum));
            currentAssets.getPrepaymentsAndOtherShortTermAssets().ifPresent(
                    prepayments -> assetMap.merge(BalanceSheetCategories.OTHER, prepayments.intValue(), Integer::sum));
        });
        assets.getNonCurrentAssets().ifPresent(nonCurrentAssets -> {
            nonCurrentAssets.getFinancialAssets().ifPresent(
                    financialAssets -> assetMap.merge(BalanceSheetCategories.FINANCIAL_ASSETS, financialAssets.intValue(), Integer::sum));
            nonCurrentAssets.getIntangibleAssets().ifPresent(
                    intangibleAssets -> assetMap.merge(BalanceSheetCategories.OTHER, intangibleAssets.intValue(), Integer::sum));
            nonCurrentAssets.getInvestments().ifPresent(
                    investments -> assetMap.merge(BalanceSheetCategories.OTHER, investments.intValue(), Integer::sum));
            nonCurrentAssets.getPropertyPlantEquipment().ifPresent(
                    propertyPlantEquipment -> assetMap.merge(BalanceSheetCategories.OTHER, propertyPlantEquipment.intValue(), Integer::sum));
        });
    }
}
