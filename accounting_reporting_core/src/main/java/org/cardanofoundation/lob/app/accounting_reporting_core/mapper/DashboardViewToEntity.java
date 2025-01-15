package org.cardanofoundation.lob.app.accounting_reporting_core.mapper;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.DashboardEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.DashboardView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardViewToEntity {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "xPos", source = "dashboardView.XPos")
    @Mapping(target = "yPos", source = "dashboardView.YPos")
    DashboardEntity mapToDashboardEntity(DashboardView dashboardView, String organisationID);

}
