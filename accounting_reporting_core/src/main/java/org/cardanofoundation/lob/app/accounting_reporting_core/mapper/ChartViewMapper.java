package org.cardanofoundation.lob.app.accounting_reporting_core.mapper;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.ChartEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.ChartView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.annotation.MatchesPattern;

@Mapper(componentModel = "spring")
public interface ChartViewMapper {

    @Mapping(target = "xPos", source = "chartView.XPos")
    @Mapping(target = "yPos", source = "chartView.YPos")
    // ID is set by the database
    @Mapping(target = "id", ignore = true)
    // Dashboard is set by the DashboardViewMapper
    @Mapping(target = "dashboard", ignore = true)
    ChartEntity toChartEntity(ChartView chartView);

}
