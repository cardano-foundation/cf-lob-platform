package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccountSubType;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationChartOfAccountSubTypeRepository extends JpaRepository<OrganisationChartOfAccountSubType, String> {

}
