package org.cardanofoundation.lob.app.blockchain_reader.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;

@Configuration
@Slf4j
public class CardanoConfig {

    @Bean
    public CardanoNetwork cardanoNetwork(@Value("${lob.cardano.network:main}") CardanoNetwork network) {
        log.info("Configured backend network: {}", network);

        return network;
    }

}
