package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.entity.*;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationChartOfAccountSubTypeRepository;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationChartOfAccountTypeRepository;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationRepository;
import org.cardanofoundation.lob.app.organisation.repository.ProjectMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

}
