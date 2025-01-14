package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.exception.MetricNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class MetricExecutor {

    protected Map<MetricEnum.SubMetric, MetricFunction> metrics;

    @Getter
    protected MetricEnum name;

    public List<MetricEnum.SubMetric> getAvailableMetrics() {
        return Optional.ofNullable(metrics)
                .orElseThrow(() -> new MetricNotFoundException(String.format("Metrics %s not initialized", name)))
                .keySet().stream().toList();
    }

    public Object getData(MetricEnum.SubMetric id, String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        Map<MetricEnum.SubMetric, MetricFunction> metricsNotInitialized = Optional.ofNullable(metrics).orElseThrow(() -> new MetricNotFoundException("Metrics not initialized"));
        MetricFunction metricFunction = metricsNotInitialized.getOrDefault(id, (String orgId, Optional<LocalDate> start, Optional<LocalDate> end) -> {
            throw new MetricNotFoundException(String.format("Metric Function %s not found in %s", id.toString(), name));
        });
        return metricFunction.getData(organisationID, startDate, endDate);
    }

}
