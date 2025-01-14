package org.cardanofoundation.lob.app.organisation.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCostCenter;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationRepository;
import org.cardanofoundation.lob.app.organisation.repository.ProjectMappingRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final CostCenterService costCenterService;
    private final ProjectMappingRepository projectMappingRepository;

    public Optional<Organisation> findById(String organisationId) {
        return organisationRepository.findById(organisationId);
    }

    public List<Organisation> findAll() {
        return organisationRepository.findAll();
    }

    public Set<OrganisationCostCenter> getAllCostCenter(String organisationId){
        return costCenterService.getAllCostCenter(organisationId);
    }

    public Set<OrganisationProject> getAllProjects(String organisationId){
        return projectMappingRepository.findAllByOrganisationId(organisationId);
    }

}
