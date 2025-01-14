package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.exception.MetricNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService{

    private final List<MetricExecutor> metricExecutors;

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

    private MetricExecutor getMetricExecutor(MetricEnum metricName) {
        return metricExecutors.stream()
                .filter(metricExecutorInterface -> metricExecutorInterface.getName().equals(metricName))
                .findFirst()
                .orElseThrow(() -> new MetricNotFoundException(String.format("Metric %s not found", metricName)));
    }


}
