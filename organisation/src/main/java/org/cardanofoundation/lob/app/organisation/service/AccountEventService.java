package org.cardanofoundation.lob.app.organisation.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.organisation.domain.entity.AccountEvent;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationAwareId;
import org.cardanofoundation.lob.app.organisation.repository.AccountEventRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AccountEventService {

    private final AccountEventRepository eventCodeRepository;

    public Optional<AccountEvent> findById(String organisationId, String customerCode) {
        return eventCodeRepository.findById(new OrganisationAwareId(organisationId, customerCode));
    }

}
