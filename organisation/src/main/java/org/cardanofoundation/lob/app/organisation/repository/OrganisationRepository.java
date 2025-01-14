package org.cardanofoundation.lob.app.organisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, String> {

}
