package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.springframework.stereotype.Service;

@Service("accounting_reporting_core.OrganisationConverter")
@Slf4j
@RequiredArgsConstructor
public class OrganisationConverter {

    public Organisation convert(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation org) {
        return Organisation.builder()
                .id(org.getId())
                .name(org.getName())
                .taxIdNumber(org.getTaxIdNumber())
                .countryCode(org.getCountryCode())
                .currencyId(org.getCurrencyId()) // TODO: Check if this is correct
                .adminEmail(org.getAdminEmail())
                .build();
    }

    public org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation convert(Organisation organisation) {
        return org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation.builder()
                .id(organisation.getId())
                .name(organisation.getName().orElse(null))
                .taxIdNumber(organisation.getTaxIdNumber().orElse(null))
                .countryCode(organisation.getCountryCode().orElse(null))
                .currencyId(organisation.getCurrencyId()) // TODO: Check if this is correct
                .adminEmail(organisation.getAdminEmail().orElse(null))
                .build();


    }

}
