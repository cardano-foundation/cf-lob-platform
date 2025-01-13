package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics;

import java.util.Date;

@FunctionalInterface
public interface MetricFunction {

    Object getData(Date startDate, Date endDate);

}
