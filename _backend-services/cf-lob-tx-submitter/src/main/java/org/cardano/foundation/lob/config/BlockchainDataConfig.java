package org.cardano.foundation.lob.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import lombok.extern.slf4j.Slf4j;
import org.cardano.foundation.lob.service.blockchain_state.*;
import org.cardano.foundation.lob.service.transaction_submit.BackendServiceBlockchainTransactionSubmissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BlockchainDataConfig {

    @Bean
    public BlockchainTransactionSubmissionService backendServiceTransactionSubmissionService(BackendService backendService) {
        return new BackendServiceBlockchainTransactionSubmissionService(backendService);
    }

    @Bean
    public BlockchainDataChainTipService blockchainDataChainTipService(BackendService backendService) {
        return new BackendServiceBlockchainDataChainTipService(backendService);
    }

    @Bean
    public BlockchainDataTransactionDetailsService blockchainDataTransactionDetailsService(BackendService backendService) {
        return new BackendServiceBlockchainDataTransactionDetailsService(backendService);
    }

}
