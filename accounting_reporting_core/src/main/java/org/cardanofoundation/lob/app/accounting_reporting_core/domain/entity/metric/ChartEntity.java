package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounting_core_charts")
public class ChartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "dashboard_id", nullable = false)
    private DashboardEntity dashboard;
    private Double xPos;
    private Double yPos;
    private Double width;
    private Double height;
    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MetricEnum metric;
    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MetricEnum.SubMetric subMetric;

}
