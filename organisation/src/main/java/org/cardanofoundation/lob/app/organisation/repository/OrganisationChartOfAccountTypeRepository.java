package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationChartOfAccountTypeRepository extends JpaRepository<OrganisationChartOfAccountType, String> {

}
