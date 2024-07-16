package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationCurrencyRepository extends JpaRepository<OrganisationCurrency, OrganisationCurrency.Id> {
}
