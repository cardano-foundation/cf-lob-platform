package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.mapper.DashboardViewMapper;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.DashboardRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors.BalanceSheetMetricService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors.IncomeStatementMetricService;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    MetricServiceImpl metricService;

    @Mock
    BalanceSheetMetricService balanceSheetMetricService;

    @Mock
    IncomeStatementMetricService incomeStatementMetricService;
    @Mock
    DashboardRepository dashboardRepository;
    @Mock
    DashboardViewMapper dashboardViewMapper;

    @BeforeEach
    void setup() {
        metricService = new MetricServiceImpl(List.of(balanceSheetMetricService, incomeStatementMetricService), dashboardRepository, dashboardViewMapper);
    }

    @Test
    void getAvailableMEtricsTest() {
        when(balanceSheetMetricService.getName()).thenReturn(MetricEnum.BALANCE_SHEET);
        when(balanceSheetMetricService.getAvailableMetrics()).thenReturn(List.of(MetricEnum.SubMetric.BALANCE_SHEET_OVERVIEW, MetricEnum.SubMetric.ASSET_CATEGORIES));
        when(incomeStatementMetricService.getName()).thenReturn(MetricEnum.INCOME_STATEMENT);
        when(incomeStatementMetricService.getAvailableMetrics()).thenReturn(List.of(MetricEnum.SubMetric.TOTAL_EXPENSES, MetricEnum.SubMetric.INCOME_STREAMS));

        Map<MetricEnum, List<MetricEnum.SubMetric>> availableMetrics = metricService.getAvailableMetrics();

        assertThat(availableMetrics.entrySet()).hasSize(2);
        assertThat(availableMetrics).containsKey(MetricEnum.BALANCE_SHEET);
        assertThat(availableMetrics).containsEntry(MetricEnum.BALANCE_SHEET, List.of(MetricEnum.SubMetric.BALANCE_SHEET_OVERVIEW, MetricEnum.SubMetric.ASSET_CATEGORIES));
        assertThat(availableMetrics).containsKey(MetricEnum.INCOME_STATEMENT);
        assertThat(availableMetrics).containsEntry(MetricEnum.INCOME_STATEMENT, List.of(MetricEnum.SubMetric.TOTAL_EXPENSES, MetricEnum.SubMetric.INCOME_STREAMS));
    }

    @Test
    void getDataTest() {
        when(balanceSheetMetricService.getName()).thenReturn(MetricEnum.BALANCE_SHEET);
        when(balanceSheetMetricService.getData(any(), any(), any(), any())).thenReturn(List.of("data1", "data2"));

        Map<MetricEnum, List<Object>> data = metricService.getData(Map.of(MetricEnum.BALANCE_SHEET, List.of(MetricEnum.SubMetric.BALANCE_SHEET_OVERVIEW)), "org-123", null, null);

        assertThat(data.entrySet()).hasSize(1);
        assertThat(data).containsKey(MetricEnum.BALANCE_SHEET);
        assertThat(data).containsEntry(MetricEnum.BALANCE_SHEET, List.of(List.of("data1", "data2")));
    }


}
