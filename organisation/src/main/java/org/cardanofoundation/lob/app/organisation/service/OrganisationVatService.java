package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationVat;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationVatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganisationVatService {

    private final OrganisationVatRepository organisationVatRepository;

    public Optional<OrganisationVat> findByOrganisationAndCode(String organisationId, String customerCode) {
        return organisationVatRepository.findById(new OrganisationVat.Id(organisationId, customerCode));
    }

}
