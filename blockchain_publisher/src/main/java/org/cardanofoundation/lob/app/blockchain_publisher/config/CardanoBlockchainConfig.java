package org.cardanofoundation.lob.app.blockchain_publisher.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain.BackendServiceBlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain.BlockchainDataChainTipService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardanoBlockchainConfig {

    @Bean
    public BlockchainDataChainTipService blockchainDataChainTipService(BackendService backendService) {
        return new BackendServiceBlockchainDataChainTipService(backendService);
    }

}
