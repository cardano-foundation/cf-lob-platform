package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<DashboardEntity, Long> {

    List<DashboardEntity> findAllByOrganisationID(String organisationID);

    Optional<DashboardEntity> findByIdAndAndOrganisationID(Long id, String organisationID);

}
