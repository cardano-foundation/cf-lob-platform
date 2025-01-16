package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.MetricView;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetMetricDataRequest {

    String organisationID;
    MetricView metricView;
    LocalDate startDate;
    LocalDate endDate;
}