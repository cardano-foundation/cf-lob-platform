package org.cardanofoundation.lob.app.organisation;

import org.cardanofoundation.lob.app.organisation.domain.entity.*;

import java.util.List;
import java.util.Optional;

public interface OrganisationPublicApiIF {

    List<Organisation> listAll();

    Optional<Organisation> findByOrganisationId(String id);

    Optional<OrganisationCurrency> findCurrencyByCustomerCurrencyCode(String organisationId,
                                                                      String customerCurrencyCode);

    Optional<OrganisationVat> findOrganisationByVatAndCode(String organisationId, String customerCode);

    Optional<OrganisationCostCenter> findCostCenter(String organisationId, String customerCode);

    Optional<OrganisationProject> findProject(String organisationId, String customerCode);

    Optional<OrganisationChartOfAccount> getChartOfAccounts(String organisationId, String customerCode);

    Optional<AccountEvent> findEventCode(String organisationId, String customerCode);

}
