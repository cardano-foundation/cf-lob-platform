package org.cardanofoundation.lob.app.organisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;

public interface ChartOfAccountRepository extends JpaRepository<OrganisationChartOfAccount, OrganisationChartOfAccount.Id>{

}
