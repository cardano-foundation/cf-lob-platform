package org.cardanofoundation.lob.app.blockchain_reader.config;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApi;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class BlockchainReaderConfig {

    @Bean
    @ConditionalOnProperty(prefix = "lob.blockchain_reader", value = "enabled", havingValue = "true", matchIfMissing = true)
    public BlockchainReaderPublicApiIF blockchainReaderPublicApiReal(RestClient restClient, CardanoNetwork network) {
        log.info("Creating BlockchainReaderPublicApi with real YACI service, blockchain_reader enabled.");

        return new BlockchainReaderPublicApi(restClient, network);
    }

    @Bean
    @ConditionalOnProperty(prefix = "lob.blockchain_reader", value = "enabled", havingValue = "false")
    public BlockchainReaderPublicApiIF blockchainReaderPublicApiMock(CardanoNetwork network) {
        log.info("Creating BlockchainReaderPublicApi with fake YACI service, blockchain_reader disabled.");

        return new BlockchainReaderPublicApiIF.Noop(network);
    }

}
