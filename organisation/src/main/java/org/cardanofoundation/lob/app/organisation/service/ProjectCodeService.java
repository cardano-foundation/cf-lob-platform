package org.cardanofoundation.lob.app.organisation.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.cardanofoundation.lob.app.organisation.repository.ProjectMappingRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectCodeService {

    private final ProjectMappingRepository projectMappingRepository;

    public Optional<OrganisationProject> getProject(String organisationId, String customerCode) {
        return projectMappingRepository.findById(new OrganisationProject.Id(organisationId, customerCode));
    }

}
