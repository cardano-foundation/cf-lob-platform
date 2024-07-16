package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartOfAccountRepository extends JpaRepository<OrganisationChartOfAccount, OrganisationChartOfAccount.Id>{

}
