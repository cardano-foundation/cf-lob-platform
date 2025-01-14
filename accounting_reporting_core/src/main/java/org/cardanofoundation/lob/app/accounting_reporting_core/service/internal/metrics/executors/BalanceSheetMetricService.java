package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.BalanceSheetCategories;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.BalanceSheetData;
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

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum.BALANCE_SHEET;

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
        List<ReportEntity> reportEntities = reportRepository.getReportEntitiesByDateBetween(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));

        Map<BalanceSheetCategories, Integer> assetCategories = new HashMap<>();

        reportEntities.forEach(reportEntity ->
                reportEntity.getBalanceSheetReportData().flatMap(BalanceSheetData::getAssets).ifPresent(assets -> {
            assets.getCurrentAssets().ifPresent(currentAssets -> {
                assetCategories.merge(BalanceSheetCategories.CASH, currentAssets.getCashAndCashEquivalents().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
                assetCategories.merge(BalanceSheetCategories.CRYPTO_ASSETS, currentAssets.getCryptoAssets().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
            });
            assets.getNonCurrentAssets().ifPresent(nonCurrentAssets -> {
                assetCategories.merge(BalanceSheetCategories.FINANCIAL_ASSETS, nonCurrentAssets.getFinancialAssets().orElse(BigDecimal.ZERO).intValue(), Integer::sum);
            });
        }));

        return assetCategories;
    }

    private Map<BalanceSheetCategories, Map<BalanceSheetCategories, Double>> getBalanceSheetOverview(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getReportEntitiesByDateBetween(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));

        Map<BalanceSheetCategories, Map<BalanceSheetCategories, Double>> balanceSheetOverview = new HashMap<>();

        reportEntities.forEach(reportEntity -> {
            reportEntity.getBalanceSheetReportData().ifPresent(balanceSheetData -> {
                balanceSheetData.getAssets().ifPresent(assets -> {
                    Map<BalanceSheetCategories, Double> assetMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.ASSETS, new HashMap<>());
                    assets.getCurrentAssets().ifPresent(currentAssets -> {
                        BigDecimal currentAssetSum = BigDecimal.ZERO;
                        currentAssetSum = currentAssetSum.add(currentAssets.getCryptoAssets().orElse(BigDecimal.ZERO));
                        currentAssetSum = currentAssetSum.add(currentAssets.getCashAndCashEquivalents().orElse(BigDecimal.ZERO));
                        currentAssetSum = currentAssetSum.add(currentAssets.getOtherReceivables().orElse(BigDecimal.ZERO));
                        currentAssetSum = currentAssetSum.add(currentAssets.getPrepaymentsAndOtherShortTermAssets().orElse(BigDecimal.ZERO));
                        assetMap.merge(BalanceSheetCategories.CURRENT, currentAssetSum.doubleValue(), Double::sum);
                    });
                    assets.getNonCurrentAssets().ifPresent(nonCurrentAssets -> {
                        BigDecimal nonCurrentAssetSum = BigDecimal.ZERO;
                        nonCurrentAssetSum = nonCurrentAssetSum.add(nonCurrentAssets.getFinancialAssets().orElse(BigDecimal.ZERO));
                        nonCurrentAssetSum = nonCurrentAssetSum.add(nonCurrentAssets.getInvestments().orElse(BigDecimal.ZERO));
                        nonCurrentAssetSum = nonCurrentAssetSum.add(nonCurrentAssets.getIntangibleAssets().orElse(BigDecimal.ZERO));
                        nonCurrentAssetSum = nonCurrentAssetSum.add(nonCurrentAssets.getPropertyPlantEquipment().orElse(BigDecimal.ZERO));
                        assetMap.merge(BalanceSheetCategories.NON_CURRENT, nonCurrentAssetSum.doubleValue(), Double::sum);
                    });
                    balanceSheetOverview.put(BalanceSheetCategories.ASSETS, assetMap);
                });

                balanceSheetData.getLiabilities().ifPresent(liabilities -> {
                    Map<BalanceSheetCategories, Double> liabilityMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.LIABILITIES, new HashMap<>());
                    liabilities.getCurrentLiabilities().ifPresent(currentLiabilities -> {
                        BigDecimal currentLiabilitySum = BigDecimal.ZERO;
                        currentLiabilitySum = currentLiabilitySum.add(currentLiabilities.getTradeAccountsPayables().orElse(BigDecimal.ZERO));
                        currentLiabilitySum = currentLiabilitySum.add(currentLiabilities.getAccrualsAndShortTermProvisions().orElse(BigDecimal.ZERO));
                        currentLiabilitySum = currentLiabilitySum.add(currentLiabilities.getOtherCurrentLiabilities().orElse(BigDecimal.ZERO));
                        liabilityMap.merge(BalanceSheetCategories.CURRENT, currentLiabilitySum.doubleValue(), Double::sum);
                    });
                    liabilities.getNonCurrentLiabilities().ifPresent(nonCurrentLiabilities -> {
                        liabilityMap.merge(BalanceSheetCategories.NON_CURRENT, nonCurrentLiabilities.getProvisions().orElse(BigDecimal.ZERO).doubleValue(), Double::sum);
                    });
                    balanceSheetOverview.put(BalanceSheetCategories.LIABILITIES, liabilityMap);
                });
                balanceSheetData.getCapital().ifPresent(capital -> {
                    Map<BalanceSheetCategories, Double> liabilityMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.LIABILITIES, new HashMap<>());
                    liabilityMap.merge(BalanceSheetCategories.CAPITAL, capital.getCapital().orElse(BigDecimal.ZERO).doubleValue(), Double::sum);
                    balanceSheetOverview.put(BalanceSheetCategories.LIABILITIES, liabilityMap);
                });
            });
        });
        return balanceSheetOverview;
    }
}
