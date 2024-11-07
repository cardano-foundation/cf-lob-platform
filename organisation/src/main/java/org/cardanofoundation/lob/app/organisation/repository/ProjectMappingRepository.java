package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCostCenter;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ProjectMappingRepository extends JpaRepository<OrganisationProject, OrganisationProject.Id> {
    @Query("SELECT t FROM OrganisationProject t WHERE t.id.organisationId = :organisationId")
    Set<OrganisationProject> findAllByOrganisationId(@Param("organisationId") String organisationId);
}
