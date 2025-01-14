package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.NetSuiteIngestionEntity;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetsuiteGatewayService {

    private final IngestionRepository ingestionRepository;

    public Optional<NetSuiteIngestionEntity> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

}
