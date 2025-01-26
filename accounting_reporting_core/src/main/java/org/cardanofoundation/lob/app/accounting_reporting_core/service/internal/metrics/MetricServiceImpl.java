package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.DashboardEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.exception.MetricNotFoundException;
import org.cardanofoundation.lob.app.accounting_reporting_core.mapper.DashboardViewMapper;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.DashboardRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.DashboardView;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService{

    private final List<MetricExecutor> metricExecutors;
    private final DashboardRepository dashboardRepository;
    private final DashboardViewMapper dashboardViewMapper;

    @Override
    public Map<MetricEnum, List<MetricEnum.SubMetric>> getAvailableMetrics() {
        return metricExecutors.stream()
                .map(metricExecutorInterface -> Map.entry(metricExecutorInterface.getName(),
                        metricExecutorInterface.getAvailableMetrics()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<MetricEnum, List<Object>> getData(Map<MetricEnum, List<MetricEnum.SubMetric>>  metrics, String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        return metrics.entrySet().stream()
                .map(metric -> {
                    MetricExecutor metricExecutor = getMetricExecutor(metric.getKey());
                    List<Object> metricData = metric.getValue().stream().map(s -> metricExecutor.getData(s,organisationID, startDate, endDate)).toList();

                    return Map.entry(metric.getKey(), metricData);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean saveDashboard(List<DashboardView> dashboards, String organisationID) {
        List<DashboardEntity> dashboardsEntities = dashboards.stream()
                .map(dashboardView -> {
                    DashboardEntity dashboardEntity = dashboardViewMapper.mapToDashboardEntity(dashboardView, organisationID);
                    dashboardEntity.getCharts().forEach(chartEntity -> chartEntity.setDashboard(dashboardEntity));
                    return dashboardEntity;
                })
                .toList();

        List<DashboardEntity> dashboardEntities = dashboardRepository.saveAll(dashboardsEntities);
        return dashboardEntities.size() == dashboards.size();
    }

    @Override
    public List<DashboardView> getAllDashboards(String organisationID) {
        List<DashboardEntity> allByOrganisationID = dashboardRepository.findAllByOrganisationID(organisationID);
        return allByOrganisationID.stream()
                .map(dashboardViewMapper::mapToDashboardView)
                .toList();
    }

    private MetricExecutor getMetricExecutor(MetricEnum metricName) {
        return metricExecutors.stream()
                .filter(metricExecutorInterface -> metricExecutorInterface.getName().equals(metricName))
                .findFirst()
                .orElseThrow(() -> new MetricNotFoundException(String.format("Metric %s not found", metricName)));
    }

    @Override
    public void deleteDashboard(String organisationID, Long dashboardID) {
        DashboardEntity dashboardEntity = dashboardRepository.findByIdAndAndOrganisationID(dashboardID, organisationID)
                .orElseThrow(() -> new MetricNotFoundException(String.format("Dashboard %s not found", dashboardID)));
        dashboardRepository.delete(dashboardEntity);
    }

    @Override
    public void updateDashboard(DashboardView dashboard, String organisationID) {
        DashboardEntity dashboardEntity = dashboardRepository.findByIdAndAndOrganisationID(dashboard.getId(), organisationID)
                .orElseThrow(() -> new MetricNotFoundException(String.format("Dashboard %s not found", dashboard.getId())));

        DashboardEntity updatedEntity = dashboardViewMapper.mapToDashboardEntity(dashboard, organisationID);
        updatedEntity.getCharts().forEach(chartEntity -> chartEntity.setDashboard(updatedEntity));
        updatedEntity.setId(dashboardEntity.getId());
        dashboardRepository.save(updatedEntity);
    }


}
