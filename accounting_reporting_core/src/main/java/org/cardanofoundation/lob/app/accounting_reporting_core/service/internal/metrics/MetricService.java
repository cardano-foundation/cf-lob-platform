package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MetricService {

    Map<String, List<String>> getAvailableMetrics();
    Map<String, List<Object>> getData(Map<String, List<String>> metrics, String organisationID, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate);
}
