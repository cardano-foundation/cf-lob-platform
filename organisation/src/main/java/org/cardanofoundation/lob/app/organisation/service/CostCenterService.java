package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCostCenter;
import org.cardanofoundation.lob.app.organisation.repository.CostCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;

    public Optional<OrganisationCostCenter> getCostCenter(String organisationId, String customerCode) {
        return costCenterRepository.findById(new OrganisationCostCenter.Id(organisationId, customerCode));
    }

    public Set<OrganisationCostCenter> getAllCostCenter(String organisationId){
        return costCenterRepository.findAllByOrganisationId(organisationId);
    }

}
