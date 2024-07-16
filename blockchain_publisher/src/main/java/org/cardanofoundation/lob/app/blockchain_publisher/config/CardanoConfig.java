package org.cardanofoundation.lob.app.blockchain_publisher.config;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CardanoConfig {

    @Bean
    public CardanoNetwork network(@Value("${lob.cardano.network:main}") CardanoNetwork network) {
        log.info("Configured backend network:{}", network);

        return network;
    }

}
