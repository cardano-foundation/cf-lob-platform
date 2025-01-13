package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.exception.MetricNotFoundException;

import java.time.LocalDateTime;
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

    public Object getData(String id, String organisationID, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate) {
        Map<String, MetricFunction> metricsNotInitialized = Optional.ofNullable(metrics).orElseThrow(() -> new MetricNotFoundException("Metrics not initialized"));
        MetricFunction metricFunction = metricsNotInitialized.getOrDefault(id, (String orgId, Optional<LocalDateTime> start, Optional<LocalDateTime> end) -> {
            throw new MetricNotFoundException(String.format("Metric Function %s not found in %s", id, name));
        });
        return metricFunction.getData(organisationID, startDate, endDate);
    }

}
