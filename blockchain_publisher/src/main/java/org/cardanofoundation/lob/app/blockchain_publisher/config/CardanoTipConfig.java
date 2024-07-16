package org.cardanofoundation.lob.app.blockchain_publisher.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BackendServiceBlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainDataChainTipService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardanoTipConfig {

    @Bean
    public BlockchainDataChainTipService blockchainDataChainTipService(BackendService backendService) {
        return new BackendServiceBlockchainDataChainTipService(backendService);
    }

}
