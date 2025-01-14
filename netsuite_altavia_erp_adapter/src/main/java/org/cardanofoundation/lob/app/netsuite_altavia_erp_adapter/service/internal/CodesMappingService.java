package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingEntity;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.CodesMappingRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CodesMappingService {

    private final CodesMappingRepository codesMappingRepository;

    public Optional<String> getCodeMapping(String organisationId,
                                           Long internalId,
                                           CodeMappingType codeMappingType) {

        return codesMappingRepository.findById(new CodeMappingEntity.Id(organisationId, internalId, codeMappingType))
                .map(CodeMappingEntity::getCode);
    }

}
