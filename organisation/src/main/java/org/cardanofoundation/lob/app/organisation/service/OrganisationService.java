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
import org.cardanofoundation.lob.app.organisation.domain.request.OrganisationCreate;
import org.cardanofoundation.lob.app.organisation.domain.request.OrganisationUpdate;
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
    private final AccountEventRepository accountEventRepository;
    private final OrganisationCurrencyService organisationCurrencyService;

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
    public Optional<Organisation> createOrganisation(OrganisationCreate organisationCreate){

        Organisation organisationO = new Organisation();
        organisationO.setId(Organisation.id(organisationCreate.getCountryCode(), organisationCreate.getTaxIdNumber()));
        organisationO.setCountryCode(organisationCreate.getCountryCode());
        organisationO.setTaxIdNumber(organisationCreate.getTaxIdNumber());
        /**
         * Those fields are needed but at the moment we don't want to set it from UI CRUD
         */
        organisationO.setDummyAccount("0000000000");
        organisationO.setAccountPeriodDays(7305);
        Organisation organisation = getOrganisation(organisationCreate, organisationO);

        organisationRepository.saveAndFlush(organisation);
        return Optional.of(organisation);
    }

    @Transactional
    public Optional<Organisation> upsertOrganisation(Organisation organisationO, OrganisationUpdate organisationUpdate) {

        if (organisationUpdate.getName() != null) {
            organisationO.setName(organisationUpdate.getName());
        }
        if (organisationUpdate.getCity() != null) {
            organisationO.setCity(organisationUpdate.getCity());
        }
        if (organisationUpdate.getPostCode() != null) {
            organisationO.setPostCode(organisationUpdate.getPostCode());
        }
        if (organisationUpdate.getProvince() != null) {
            organisationO.setProvince(organisationUpdate.getProvince());
        }
        if (organisationUpdate.getAddress() != null) {
            organisationO.setAddress(organisationUpdate.getAddress());
        }
        if (organisationUpdate.getPhoneNumber() != null) {
            organisationO.setPhoneNumber(organisationUpdate.getPhoneNumber());
        }
        if (organisationUpdate.getAdminEmail() != null) {
            organisationO.setAdminEmail(organisationUpdate.getAdminEmail());
        }
        if (organisationUpdate.getWebsiteUrl() != null) {
            organisationO.setWebSite(organisationUpdate.getWebsiteUrl());
        }
        if (organisationUpdate.getCurrencyId() != null) {
            organisationO.setCurrencyId(organisationUpdate.getCurrencyId());
        }
        if (organisationUpdate.getReportCurrencyId() != null) {
            organisationO.setReportCurrencyId(organisationUpdate.getReportCurrencyId());
        }

        Organisation organisation = organisationRepository.saveAndFlush(organisationO);
        return Optional.of(organisation);
    }


    public OrganisationView getOrganisationView(Organisation organisation) {
        LocalDate today = LocalDate.now();
        LocalDate monthsAgo = today.minusMonths(organisation.getAccountPeriodDays());
        LocalDate yesterday = today.minusDays(1);

        return new OrganisationView(
                organisation.getId(),
                organisation.getName(),
                "", // Description is deprecated and should be removed
                organisation.getTaxIdNumber(),
                organisation.getCurrencyId(),
                organisation.getReportCurrencyId(),
                monthsAgo,
                yesterday,
                organisation.getAdminEmail(),
                organisation.getPhoneNumber(),
                organisation.getAddress(),
                organisation.getCity(),
                organisation.getPostCode(),
                organisation.getProvince(),
                organisation.getCountryCode(),
                getAllCostCenter(organisation.getId()).stream().map(organisationCostCenter ->
                        new OrganisationCostCenterView(
                            organisationCostCenter.getId() != null ? organisationCostCenter.getId().getCustomerCode() : null,
                            organisationCostCenter.getExternalCustomerCode(),
                            organisationCostCenter.getName()
                    )).collect(Collectors.toSet()),
                getAllProjects(organisation.getId()).stream().map(organisationProject ->
                        new OrganisationCostCenterView(
                            organisationProject.getId() != null ? organisationProject.getId().getCustomerCode() : null,
                            organisationProject.getExternalCustomerCode(),
                            organisationProject.getName()
                    )).collect(Collectors.toSet()),
                organisationCurrencyService.findAllByOrganisationId(organisation.getId())
                        .stream()
                        .map(organisationCurrency ->
                                organisationCurrency.getId() != null ? organisationCurrency.getId().getCustomerCode() : null
                        ).collect(Collectors.toSet()),
                organisation.getWebSite(),
                organisation.getLogo()
        );
    }

    private static Organisation getOrganisation(OrganisationCreate organisationCreate, Organisation organisation) {
        organisation.setName(organisationCreate.getName());
        organisation.setCity(organisationCreate.getCity());
        organisation.setPostCode(organisationCreate.getPostCode());
        organisation.setProvince(organisationCreate.getProvince());
        organisation.setAddress(organisationCreate.getAddress());
        organisation.setPhoneNumber(organisationCreate.getPhoneNumber());
        organisation.setAdminEmail(organisationCreate.getAdminEmail());
        organisation.setWebSite(organisationCreate.getWebsiteUrl());
        organisation.setCurrencyId(organisationCreate.getCurrencyId());
        organisation.setReportCurrencyId(organisationCreate.getReportCurrencyId());




        return organisation;
    }

}
