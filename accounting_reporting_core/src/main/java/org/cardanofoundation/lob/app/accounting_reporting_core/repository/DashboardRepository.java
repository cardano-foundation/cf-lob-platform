package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric.DashboardEntity;

public interface DashboardRepository extends JpaRepository<DashboardEntity, Long> {

    List<DashboardEntity> findAllByOrganisationID(String organisationID);

    Optional<DashboardEntity> findByIdAndAndOrganisationID(Long id, String organisationID);

}
