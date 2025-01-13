package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetricView {

    Map<String, List<String>> metrics;
    Date startDate;
    Date endDate;

    public MetricView(Map<String, List<String>> metrics) {
        this.metrics = metrics;
    }

}
