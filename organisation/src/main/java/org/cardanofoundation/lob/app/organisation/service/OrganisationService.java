package org.cardanofoundation.lob.app.organisation.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.organisation.domain.entity.*;
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
    private final OrganisationChartOfAccountSubTypeRepository organisationChartOfAccountSubTypeRepository;
    private final AccountEventRepository accountEventRepository;

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

    public Set<AccountEvent> getOrganisationEventCode(String orgId){
        return accountEventRepository.findAllByOrganisationId(orgId);
    }

}
