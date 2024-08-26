package org.cardanofoundation.lob.app.blockchain_publisher.config;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.BlockchainPublisherException;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CardanoConfig {

    @Bean
    public CardanoNetwork network(@Value("${lob.cardano.network:main}") CardanoNetwork network) {
        log.info("Configured backend network: {}", network);

        if (network == CardanoNetwork.PREVIEW) {
            throw new BlockchainPublisherException("LOB not supporting preview network just yet, needed changes are minimal though!");
        }

        return network;
    }

}
