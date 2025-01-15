package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Validable;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.springframework.data.domain.Persistable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounting_core_dashboards")
public class DashboardEntity{

    @Id
    @GeneratedValue
    private int id;

    private String name;
    private String description;
    private Double xPos;
    private Double yPos;
    private Double width;
    private Double height;
    private MetricEnum metric;
    private MetricEnum.SubMetric subMetric;
}
