package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.AccountEvent;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationAwareId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEventRepository extends JpaRepository<AccountEvent, OrganisationAwareId>{

}
