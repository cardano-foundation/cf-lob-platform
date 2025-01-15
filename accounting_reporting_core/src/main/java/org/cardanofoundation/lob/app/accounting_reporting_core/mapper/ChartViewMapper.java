package org.cardanofoundation.lob.app.accounting_reporting_core.mapper;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.ChartEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.ChartView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChartViewMapper {

    @Mapping(target = "xPos", source = "chartView.XPos")
    @Mapping(target = "yPos", source = "chartView.YPos")
    ChartEntity toChartEntity(ChartView chartView);

}
