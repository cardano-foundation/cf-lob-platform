package org.cardanofoundation.lob.app.blockchain_reader.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CardanoClientLibConfig {

    @Bean
    @Qualifier("original_blockfrost")
    public BackendService blockforstBlockchainBackendService(@Value("${lob.blockfrost.url}") String blockfrostUrl,
                                                             @Value("${lob.blockfrost.api_key}") String blockfrostApiKey) {
        return new BFBackendService(blockfrostUrl, blockfrostApiKey);
    }

    @Bean
    public Network network(CardanoNetwork chainNetwork) {
        return switch(chainNetwork) {
            case MAIN -> Networks.mainnet();
            case PREPROD -> Networks.preprod();
            case PREVIEW -> Networks.preview();
            case DEV -> Networks.testnet();
        };
    }

}
