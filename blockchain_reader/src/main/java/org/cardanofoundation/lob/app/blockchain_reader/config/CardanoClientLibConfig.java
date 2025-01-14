package org.cardanofoundation.lob.app.blockchain_reader.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;

@Configuration
@Slf4j
public class CardanoClientLibConfig {

    @Bean
    @Qualifier("original_blockfrost")
    public BackendService blockforstBlockchainBackendService(@Value("${lob.blockfrost.url}") String blockfrostUrl,
                                                             @Value("${lob.blockfrost.api_key}") String blockfrostApiKey) {
        return new BFBackendService(blockfrostUrl, blockfrostApiKey);
    }

}
