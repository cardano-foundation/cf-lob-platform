package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.exception.MetricNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService{

    private final List<MetricExecutor> metricExecutors;

    public Map<String, List<String>> getAvailableMetrics() {
        return metricExecutors.stream()
                .map(metricExecutorInterface -> Map.entry(metricExecutorInterface.getName(),
                        metricExecutorInterface.getAvailableMetrics()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, List<Object>> getData(Map<String, List<String>> metrics) {
        return metrics.entrySet().stream()
                .map(metric -> {
                    MetricExecutor metricExecutor = getMetricExecutor(metric.getKey());
                    List<Object> metricData = metric.getValue().stream().map(metricExecutor::getData).toList();

                    return Map.entry(metric.getKey(), metricData);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private MetricExecutor getMetricExecutor(String metricName) {
        return metricExecutors.stream()
                .filter(metricExecutorInterface -> metricExecutorInterface.getName().equals(metricName))
                .findFirst()
                .orElseThrow(() -> new MetricNotFoundException(String.format("Metric %s not found", metricName)));
    }


}
