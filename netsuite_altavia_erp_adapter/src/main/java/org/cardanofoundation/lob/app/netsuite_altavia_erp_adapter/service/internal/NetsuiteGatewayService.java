package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.NetSuiteIngestionEntity;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetsuiteGatewayService {

    private final IngestionRepository ingestionRepository;

    public Optional<NetSuiteIngestionEntity> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

}
