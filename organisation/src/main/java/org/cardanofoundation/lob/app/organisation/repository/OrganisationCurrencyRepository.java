package org.cardanofoundation.lob.app.organisation.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;

public interface OrganisationCurrencyRepository extends JpaRepository<OrganisationCurrency, OrganisationCurrency.Id> {

    @Query("SELECT t FROM OrganisationCurrency t WHERE t.id.organisationId = :organisationId")
    Set<OrganisationCurrency> findAllByOrganisationId(@Param("organisationId") String organisationId);

}
