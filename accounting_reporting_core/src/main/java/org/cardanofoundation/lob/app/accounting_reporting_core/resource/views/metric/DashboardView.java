package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardView {

    private Long id;
    private String name;
    private String description;
    private List<ChartView> charts;
}
