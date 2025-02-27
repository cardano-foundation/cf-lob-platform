package org.cardanofoundation.lob.app.organisation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.organisation.domain.entity.*;
import org.cardanofoundation.lob.app.organisation.domain.request.OrganisationUpsert;
import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationCostCenterView;
import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationView;
import org.cardanofoundation.lob.app.organisation.repository.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final CostCenterService costCenterService;
    private final ProjectMappingRepository projectMappingRepository;
    private final OrganisationChartOfAccountTypeRepository organisationChartOfAccountTypeRepository;
    private final ChartOfAccountRepository organisationChartOfAccountRepository;
    private final OrganisationChartOfAccountSubTypeRepository organisationChartOfAccountSubTypeRepository;
    private final AccountEventRepository accountEventRepository;
    private final OrganisationCurrencyService organisationCurrencyService;
    private final OrganisationChartOfAccountSubTypeRepository chartOfAccountSubTypeRepository;


    public Optional<Organisation> findById(String organisationId) {
        return organisationRepository.findById(organisationId);
    }

    public List<Organisation> findAll() {
        return organisationRepository.findAll();
    }

    public Set<OrganisationCostCenter> getAllCostCenter(String organisationId) {
        return costCenterService.getAllCostCenter(organisationId);
    }

    public Set<OrganisationProject> getAllProjects(String organisationId) {
        return projectMappingRepository.findAllByOrganisationId(organisationId);
    }

    @Transactional
    public Set<OrganisationChartOfAccountType> getAllChartType(String organisationId) {
        return organisationChartOfAccountTypeRepository.findAllByOrganisationId(organisationId);
    }

    public Set<OrganisationChartOfAccount> getBySubTypeId(Long subType) {
        return organisationChartOfAccountRepository.findAllByOrganisationIdSubTypeId(subType);
    }

    public Set<AccountEvent> getOrganisationEventCode(String orgId) {
        return accountEventRepository.findAllByOrganisationId(orgId);
    }

    @Transactional
    public OrganisationView upsertOrganisation(OrganisationUpsert organisationUpsert) {
        Optional<Organisation> organisationO = findById(Organisation.id(organisationUpsert.getCountryCode(), organisationUpsert.getTaxIdNumber()));
        if (organisationO.isEmpty()) {
            organisationO = Optional.of(new Organisation());
            organisationO.get().setId(Organisation.id(organisationUpsert.getCountryCode(), organisationUpsert.getTaxIdNumber()));
            organisationO.get().setCountryCode(organisationUpsert.getCountryCode());
            organisationO.get().setTaxIdNumber(organisationUpsert.getTaxIdNumber());
            /**
             * Those fields are needed but at the moment we don't want to set it from UI CRUD
             */
            organisationO.get().setDummyAccount("0000000000");
            organisationO.get().setAccountPeriodDays(7305);
        }


        Organisation organisation = getOrganisation(organisationUpsert, organisationO);

        organisationRepository.saveAndFlush(organisation);
        return getOrganisationView(organisation);
    }


    public OrganisationView getOrganisationView(Organisation organisation) {
        LocalDate today = LocalDate.now();
        LocalDate monthsAgo = today.minusMonths(organisation.getAccountPeriodDays());
        LocalDate yesterday = today.minusDays(1);

        return new OrganisationView(
                organisation.getId(),
                organisation.getName(),
                organisation.getTaxIdNumber(),
                organisation.getTaxIdNumber(),
                organisation.getCurrencyId(),
                monthsAgo,
                yesterday,
                organisation.getAdminEmail(),
                organisation.getPhoneNumber(),
                organisation.getAddress(),
                organisation.getCity(),
                organisation.getPostCode(),
                organisation.getProvince(),
                organisation.getCountry(),
                getAllCostCenter(organisation.getId()).stream().map(organisationCostCenter -> {
                    return new OrganisationCostCenterView(
                            organisationCostCenter.getId() != null ? organisationCostCenter.getId().getCustomerCode() : null,
                            organisationCostCenter.getExternalCustomerCode(),
                            organisationCostCenter.getName()
                    );
                }).collect(Collectors.toSet()),
                getAllProjects(organisation.getId()).stream().map(organisationProject -> {
                    return new OrganisationCostCenterView(
                            organisationProject.getId() != null ? organisationProject.getId().getCustomerCode() : null,
                            organisationProject.getExternalCustomerCode(),
                            organisationProject.getName()
                    );
                }).collect(Collectors.toSet()),
                organisationCurrencyService.findAllByOrganisationId(organisation.getId())
                        .stream()
                        .map(organisationCurrency -> {
                            return organisationCurrency.getId() != null ? organisationCurrency.getId().getCustomerCode() : null;
                        }).collect(Collectors.toSet()),
                organisation.getLogo()
        );
    }

    private static Organisation getOrganisation(OrganisationUpsert organisationUpsert, Optional<Organisation> organisationO) {
        Organisation organisation = organisationO.get();
        organisation.setName(organisationUpsert.getName());
        organisation.setCity(organisationUpsert.getCity());
        organisation.setPostCode(organisationUpsert.getPostCode());
        organisation.setProvince(organisationUpsert.getProvince());
        organisation.setCountry(organisationUpsert.getCountry());
        organisation.setAddress(organisationUpsert.getAddress());
        organisation.setCurrencyId(organisationUpsert.getCurrencyId());
        organisation.setReportCurrencyId(organisationUpsert.getReportCurrencyId());
        organisation.setPhoneNumber(organisationUpsert.getPhoneNumber());
        organisation.setAdminEmail(organisationUpsert.getAdminEmail());
        organisation.setWebSite(organisationUpsert.getWebsiteUrl());
        return organisation;
    }

}
