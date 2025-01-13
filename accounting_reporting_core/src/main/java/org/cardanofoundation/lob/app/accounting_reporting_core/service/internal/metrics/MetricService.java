package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MetricService {

    Map<String, List<String>> getAvailableMetrics();
    Map<String, List<Object>> getData(Map<String, List<String>> metrics, Date startDate, Date endDate);
}
