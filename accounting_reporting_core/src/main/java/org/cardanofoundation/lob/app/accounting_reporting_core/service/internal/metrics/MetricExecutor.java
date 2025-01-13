package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.exception.MetricNotFoundException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class MetricExecutor {

    protected Map<String, MetricFunction> metrics;

    @Getter
    protected String name;

    public List<String> getAvailableMetrics() {
        return Optional.ofNullable(metrics)
                .orElseThrow(() -> new MetricNotFoundException(String.format("Metrics %s not initialized", name)))
                .keySet().stream().toList();
    }

    public Object getData(String id, Date startDate, Date endDate) {
        Map<String, MetricFunction> metricsNotInitialized = Optional.ofNullable(metrics).orElseThrow(() -> new MetricNotFoundException("Metrics not initialized"));
        MetricFunction metricFunction = metricsNotInitialized.getOrDefault(id, () -> {
            throw new MetricNotFoundException(String.format("Metric Function %s not found in %s", id, name));
        });
        return metricFunction.getData(startDate, endDate);
    }

}
