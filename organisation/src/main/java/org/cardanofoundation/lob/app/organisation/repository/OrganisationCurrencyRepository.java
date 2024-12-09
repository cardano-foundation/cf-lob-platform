package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCostCenter;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface OrganisationCurrencyRepository extends JpaRepository<OrganisationCurrency, OrganisationCurrency.Id> {

    @Query("SELECT t FROM OrganisationCurrency t WHERE t.id.organisationId = :organisationId")
    Set<OrganisationCurrency> findAllByOrganisationId(@Param("organisationId") String organisationId);

}
