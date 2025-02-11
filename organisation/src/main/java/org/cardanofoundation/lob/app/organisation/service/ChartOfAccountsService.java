package org.cardanofoundation.lob.app.organisation.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;
import org.cardanofoundation.lob.app.organisation.repository.ChartOfAccountRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChartOfAccountsService {

    private final ChartOfAccountRepository chartOfAccountRepository;

    public Optional<OrganisationChartOfAccount> getChartAccount(String organisationId, String customerCode) {
        return chartOfAccountRepository.findById(new OrganisationChartOfAccount.Id(organisationId, customerCode));
    }

}
