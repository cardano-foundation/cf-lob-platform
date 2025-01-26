package org.cardanofoundation.lob.app.accounting_reporting_core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.DashboardEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.DashboardView;

@Mapper(componentModel = "spring", uses = ChartViewMapper.class)
public interface DashboardViewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organisationID", source = "organisationID")
    @Mapping(target = "charts", source = "dashboardView.charts")
    DashboardEntity mapToDashboardEntity(DashboardView dashboardView, String organisationID);

    DashboardView mapToDashboardView(DashboardEntity dashboardEntity);

}
