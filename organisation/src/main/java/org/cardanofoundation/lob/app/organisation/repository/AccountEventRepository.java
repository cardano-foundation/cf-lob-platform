package org.cardanofoundation.lob.app.organisation.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.lob.app.organisation.domain.entity.AccountEvent;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationAwareId;

public interface AccountEventRepository extends JpaRepository<AccountEvent, OrganisationAwareId>{
    @Query("SELECT a FROM AccountEvent a " +
            "WHERE a.id.organisationId = :organisationId")
    Set<AccountEvent> findAllByOrganisationId(@Param("organisationId") String organisationId);
}
