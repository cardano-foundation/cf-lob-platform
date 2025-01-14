package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository;

import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodesMappingRepository extends JpaRepository<CodeMappingEntity, CodeMappingEntity.Id> {

}
