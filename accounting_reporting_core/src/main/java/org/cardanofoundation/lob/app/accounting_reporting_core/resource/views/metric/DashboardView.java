package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
