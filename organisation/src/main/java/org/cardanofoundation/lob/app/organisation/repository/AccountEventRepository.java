package org.cardanofoundation.lob.app.organisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.organisation.domain.entity.AccountEvent;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationAwareId;

public interface AccountEventRepository extends JpaRepository<AccountEvent, OrganisationAwareId>{

}
