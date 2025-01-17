package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MetricService {

    Map<MetricEnum, List<MetricEnum.SubMetric>> getAvailableMetrics();
    Map<MetricEnum, List<Object>> getData(Map<MetricEnum, List<MetricEnum.SubMetric>>  metrics, String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate);
}
