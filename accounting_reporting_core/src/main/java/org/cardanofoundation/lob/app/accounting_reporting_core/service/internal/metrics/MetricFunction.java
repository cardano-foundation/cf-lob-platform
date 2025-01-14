package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import java.time.LocalDate;
import java.util.Optional;

@FunctionalInterface
public interface MetricFunction {

    Object getData(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate);

}
