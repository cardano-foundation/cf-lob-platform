package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.BalanceSheetCategories;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.BalanceSheetData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class BalanceSheetMetricServiceTest {

    @Mock
    ReportRepository reportRepository;

    BalanceSheetMetricService balanceSheetMetricService;

    @BeforeEach
    void setup() {
        balanceSheetMetricService = new BalanceSheetMetricService(reportRepository);
        balanceSheetMetricService.init();
    }

    @Test
    void getAssetCategoriesTest() {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setType(ReportType.BALANCE_SHEET);
        reportEntity.setBalanceSheetReportData(Optional.of(getTestBalanceSheetData()));

        when(reportRepository.getNewestReportsInRange(anyString(), any(), any()))
                .thenReturn(List.of(reportEntity));

        Map<BalanceSheetCategories, Integer> assetCategories = (Map<BalanceSheetCategories, Integer>) balanceSheetMetricService.getData(MetricEnum.SubMetric.ASSET_CATEGORIES, "organisationId", Optional.empty(), Optional.empty());

        assertThat(assetCategories).containsEntry(BalanceSheetCategories.CASH, 10);
        assertThat(assetCategories).containsEntry(BalanceSheetCategories.CRYPTO_ASSETS, 10);
        assertThat(assetCategories).containsEntry(BalanceSheetCategories.FINANCIAL_ASSETS, 10);
    }

    @Test
    void getBalanceSheetOverviewTest() {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setType(ReportType.BALANCE_SHEET);
        reportEntity.setBalanceSheetReportData(Optional.of(getTestBalanceSheetData()));

        when(reportRepository.getNewestReportsInRange(anyString(), any(), any()))
                .thenReturn(List.of(reportEntity));

        Map<BalanceSheetCategories, Map<BalanceSheetCategories, Integer>> balanceSheetOverview = (Map<BalanceSheetCategories, Map<BalanceSheetCategories, Integer>>) balanceSheetMetricService.getData(MetricEnum.SubMetric.BALANCE_SHEET_OVERVIEW, "organisationId", Optional.empty(), Optional.empty());

        assertThat(balanceSheetOverview).containsKey(BalanceSheetCategories.ASSETS);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.ASSETS)).containsEntry(BalanceSheetCategories.FINANCIAL_ASSETS, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.ASSETS)).containsEntry(BalanceSheetCategories.CASH, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.ASSETS)).containsEntry(BalanceSheetCategories.CRYPTO_ASSETS, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.ASSETS)).containsEntry(BalanceSheetCategories.OTHER, 50);
        assertThat(balanceSheetOverview).containsKey(BalanceSheetCategories.LIABILITIES);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.PROFIT_OF_THE_YEAR, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.ACCRUSAL_AND_SHORT_TERM_PROVISIONS, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.TRADE_ACCOUNTS_PAYABLE, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.RESULTS_CARRIED_FORWARD, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.CAPITAL, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.OTHER, 10);
        assertThat(balanceSheetOverview.get(BalanceSheetCategories.LIABILITIES)).containsEntry(BalanceSheetCategories.PROVISIONS, 10);
    }

    private BalanceSheetData getTestBalanceSheetData() {
        return BalanceSheetData.builder()
                .assets(BalanceSheetData.Assets.builder()
                        .currentAssets(BalanceSheetData.Assets.CurrentAssets.builder()
                                .cashAndCashEquivalents(BigDecimal.TEN)
                                .cryptoAssets(BigDecimal.TEN)
                                .otherReceivables(BigDecimal.TEN)
                                .prepaymentsAndOtherShortTermAssets(BigDecimal.TEN)
                                .build())
                        .nonCurrentAssets(BalanceSheetData.Assets.NonCurrentAssets.builder()
                                .financialAssets(BigDecimal.TEN)
                                .intangibleAssets(BigDecimal.TEN)
                                .investments(BigDecimal.TEN)
                                .propertyPlantEquipment(BigDecimal.TEN)
                                .build())
                        .build())
                .capital(BalanceSheetData.Capital.builder()
                        .capital(BigDecimal.TEN)
                        .profitForTheYear(BigDecimal.TEN)
                        .resultsCarriedForward(BigDecimal.TEN)
                        .build())
                .liabilities(BalanceSheetData.Liabilities.builder()
                        .currentLiabilities(BalanceSheetData.Liabilities.CurrentLiabilities.builder()
                                .accrualsAndShortTermProvisions(BigDecimal.TEN)
                                .otherCurrentLiabilities(BigDecimal.TEN)
                                .tradeAccountsPayables(BigDecimal.TEN)
                                .build())
                        .nonCurrentLiabilities(BalanceSheetData.Liabilities.NonCurrentLiabilities.builder()
                                .provisions(BigDecimal.TEN)
                                .build())
                        .build())
                .build();
    }


}
