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
import java.util.stream.Stream;

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
                currentAssets.getCashAndCashEquivalents().ifPresent(cash -> {
                    assetCategories.merge(BalanceSheetCategories.CASH, cash.intValue(), Integer::sum);
                });
                currentAssets.getCryptoAssets().ifPresent(cryptoAssets -> {
                    assetCategories.merge(BalanceSheetCategories.CRYPTO_ASSETS, cryptoAssets.intValue(), Integer::sum);
                });
                currentAssets.getOtherReceivables().ifPresent(otherReceivables -> {
                    assetCategories.merge(BalanceSheetCategories.OTHER, otherReceivables.intValue(), Integer::sum);
                });
                currentAssets.getPrepaymentsAndOtherShortTermAssets().ifPresent(prepayments -> {
                    assetCategories.merge(BalanceSheetCategories.OTHER, prepayments.intValue(), Integer::sum);
                });
            });

            assets.getNonCurrentAssets().ifPresent(nonCurrentAssets -> {
                nonCurrentAssets.getFinancialAssets().ifPresent(financialAssets -> {
                    assetCategories.merge(BalanceSheetCategories.FINANCIAL_ASSETS, financialAssets.intValue(), Integer::sum);
                });
                nonCurrentAssets.getIntangibleAssets().ifPresent(intangibleAssets -> {
                    assetCategories.merge(BalanceSheetCategories.OTHER, intangibleAssets.intValue(), Integer::sum);
                });
                nonCurrentAssets.getInvestments().ifPresent(investments -> {
                    assetCategories.merge(BalanceSheetCategories.OTHER, investments.intValue(), Integer::sum);
                });
                nonCurrentAssets.getPropertyPlantEquipment().ifPresent(propertyPlantEquipment -> {
                    assetCategories.merge(BalanceSheetCategories.OTHER, propertyPlantEquipment.intValue(), Integer::sum);
                });
            });
        }));

        return assetCategories;
    }

    private Map<BalanceSheetCategories, Map<BalanceSheetCategories, Integer>> getBalanceSheetOverview(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        List<ReportEntity> reportEntities = reportRepository.getReportEntitiesByDateBetween(organisationID,
                startDate.orElse(null),
                endDate.orElse(null));

        Map<BalanceSheetCategories, Map<BalanceSheetCategories, Integer>> balanceSheetOverview = new HashMap<>();

        reportEntities.forEach(reportEntity -> {
            reportEntity.getBalanceSheetReportData().ifPresent(balanceSheetData -> {
                balanceSheetData.getAssets().ifPresent(assets -> {
                    Map<BalanceSheetCategories, Integer> assetMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.ASSETS, new HashMap<>());
                    assets.getCurrentAssets().ifPresent(currentAssets -> {
                        int currentAssetSum = sumUpOptionalFields(
                                currentAssets.getCryptoAssets(),
                                currentAssets.getCashAndCashEquivalents(), currentAssets.getOtherReceivables(),
                                currentAssets.getPrepaymentsAndOtherShortTermAssets());
                        assetMap.merge(BalanceSheetCategories.CURRENT, currentAssetSum, Integer::sum);
                    });
                    assets.getNonCurrentAssets().ifPresent(nonCurrentAssets -> {
                        int nonCurrentAssetSum = sumUpOptionalFields(
                                nonCurrentAssets.getFinancialAssets(),
                                nonCurrentAssets.getInvestments(),
                                nonCurrentAssets.getIntangibleAssets(),
                                nonCurrentAssets.getPropertyPlantEquipment());
                        assetMap.merge(BalanceSheetCategories.NON_CURRENT, nonCurrentAssetSum, Integer::sum);
                    });
                    balanceSheetOverview.put(BalanceSheetCategories.ASSETS, assetMap);
                });

                balanceSheetData.getLiabilities().ifPresent(liabilities -> {
                    Map<BalanceSheetCategories, Integer> liabilityMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.LIABILITIES, new HashMap<>());
                    liabilities.getCurrentLiabilities().ifPresent(currentLiabilities -> {
                        int currentLiabilitySum = sumUpOptionalFields(
                                currentLiabilities.getTradeAccountsPayables(),
                                currentLiabilities.getAccrualsAndShortTermProvisions(),
                                currentLiabilities.getOtherCurrentLiabilities());
                        liabilityMap.merge(BalanceSheetCategories.CURRENT, currentLiabilitySum, Integer::sum);
                    });
                    liabilities.getNonCurrentLiabilities().ifPresent(nonCurrentLiabilities -> {
                        int nonCurrentLiabilitySum = sumUpOptionalFields(
                                nonCurrentLiabilities.getProvisions());
                        liabilityMap.merge(BalanceSheetCategories.NON_CURRENT, nonCurrentLiabilitySum, Integer::sum);
                    });
                    balanceSheetOverview.put(BalanceSheetCategories.LIABILITIES, liabilityMap);
                });
                balanceSheetData.getCapital().ifPresent(capital -> {
                    Map<BalanceSheetCategories, Integer> liabilityMap = balanceSheetOverview.getOrDefault(BalanceSheetCategories.LIABILITIES, new HashMap<>());
                    int capitalSum = sumUpOptionalFields(
                            capital.getCapital());
                    liabilityMap.merge(BalanceSheetCategories.CAPITAL, capitalSum, Integer::sum);
                    balanceSheetOverview.put(BalanceSheetCategories.LIABILITIES, liabilityMap);
                });
            });
        });
        return balanceSheetOverview;
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
