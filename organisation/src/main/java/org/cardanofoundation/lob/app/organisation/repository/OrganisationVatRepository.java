package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationVat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationVatRepository extends JpaRepository<OrganisationVat, OrganisationVat.Id> {

}
