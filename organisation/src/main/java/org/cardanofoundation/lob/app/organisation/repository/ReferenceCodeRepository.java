package org.cardanofoundation.lob.app.organisation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.cardanofoundation.lob.app.organisation.domain.entity.ReferenceCode;

public interface ReferenceCodeRepository extends JpaRepository<ReferenceCode, ReferenceCode.Id> {

    @Query("SELECT rc FROM ReferenceCode rc WHERE rc.id.organisationId = :orgId")
    List<ReferenceCode> findAllByOrgId(String orgId);

    @Query("SELECT rc FROM ReferenceCode rc WHERE rc.id.organisationId = :orgId AND rc.id.referenceCode = :referenceCode")
    Optional<ReferenceCode> findByOrgIdAndReferenceCode(String orgId, String referenceCode);
}
