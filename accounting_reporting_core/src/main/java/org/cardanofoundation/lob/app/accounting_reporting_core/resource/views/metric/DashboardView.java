package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardView {

    private String name;
    private String description;
    private String userID;
    private Double xPos;
    private Double yPos;
    private Double width;
    private Double height;
    private MetricEnum metric;
    private MetricEnum.SubMetric subMetric;
}
