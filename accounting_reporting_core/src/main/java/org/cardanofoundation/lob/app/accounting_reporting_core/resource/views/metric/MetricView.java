package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetricView {

    Map<MetricEnum, List<MetricEnum.SubMetric>> metrics;

}
