package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingEntity;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.CodesMappingRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CodesMappingService {

    private final CodesMappingRepository codesMappingRepository;

    @Cacheable(value = "netsuiteCodesMapping")
    public Optional<String> getCodeMapping(String organisationId, Long internalId, CodeMappingType codeMappingType) {
        return codesMappingRepository.findById(new CodeMappingEntity.Id(organisationId, internalId, codeMappingType))
                .map(CodeMappingEntity::getCode);
    }

    @Scheduled(fixedRateString = "PT5M")
    @CacheEvict(value = "netsuiteCodesMapping", allEntries = true)
    public void evictNetsuiteCodesCache() {
        log.info("Evicting cache for Netsuite Codes Mapping...");
    }

}
