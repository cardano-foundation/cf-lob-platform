package org.cardanofoundation.lob.app.organisation.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccountSubType;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccountType;

public interface OrganisationChartOfAccountSubTypeRepository extends JpaRepository<OrganisationChartOfAccountSubType, String> {
    @Query("SELECT st FROM OrganisationChartOfAccountSubType st " +
            "WHERE st.type = :type")
    Set<OrganisationChartOfAccountSubType> findAllByType(@Param("type") OrganisationChartOfAccountType type);
}
