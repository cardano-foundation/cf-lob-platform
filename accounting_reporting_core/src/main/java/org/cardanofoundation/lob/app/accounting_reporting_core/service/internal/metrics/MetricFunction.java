package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import java.time.LocalDateTime;
import java.util.Optional;

@FunctionalInterface
public interface MetricFunction {

    Object getData(String organisationID, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate);

}
