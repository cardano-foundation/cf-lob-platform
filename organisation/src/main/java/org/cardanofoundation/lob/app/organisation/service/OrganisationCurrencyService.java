package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationCurrencyRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganisationCurrencyService {

    private final OrganisationCurrencyRepository organisationCurrencyRepository;

    public Optional<OrganisationCurrency> findByOrganisationIdAndCode(@Param("organisationId") String organisationId,
                                                                      @Param("customerCode") String customerCode) {
        return organisationCurrencyRepository.findById(new OrganisationCurrency.Id(organisationId, customerCode));
    }

}
