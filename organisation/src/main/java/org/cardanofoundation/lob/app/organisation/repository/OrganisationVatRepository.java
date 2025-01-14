package org.cardanofoundation.lob.app.organisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationVat;

public interface OrganisationVatRepository extends JpaRepository<OrganisationVat, OrganisationVat.Id> {

}
