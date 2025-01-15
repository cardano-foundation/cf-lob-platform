package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardRepository extends JpaRepository<DashboardEntity, String> {

    List<DashboardEntity> findAllByOrganisationID(String organisationID);

}
