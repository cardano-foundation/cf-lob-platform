package org.cardanofoundation.lob.app.organisation.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;

public interface ChartOfAccountRepository extends JpaRepository<OrganisationChartOfAccount, OrganisationChartOfAccount.Id>{

    @Query("SELECT t FROM OrganisationChartOfAccount t " +
            "WHERE t.subType.id = :subTypeId")
    Set<OrganisationChartOfAccount> findAllByOrganisationIdSubTypeId(@Param("subTypeId") Long subTypeId);
}
