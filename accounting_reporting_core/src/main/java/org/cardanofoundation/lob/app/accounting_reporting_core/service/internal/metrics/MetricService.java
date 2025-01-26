package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.DashboardView;

public interface MetricService {

    Map<MetricEnum, List<MetricEnum.SubMetric>> getAvailableMetrics();

    Map<MetricEnum, List<Object>> getData(Map<MetricEnum, List<MetricEnum.SubMetric>> metrics, String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate);

    boolean saveDashboard(List<DashboardView> dashboards, String organisationID);

    List<DashboardView> getAllDashboards(String organisationID);

    void deleteDashboard(String organisationID, Long dashboardID);

    void updateDashboard(DashboardView dashboard, String organisationID);
}
