package org.cardanofoundation.lob.app.blockchain_reader.config;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("blockchain_reader.cardano_config")
@Slf4j
public class CardanoConfig {

    @Bean("blockchain_reader.network")
    public CardanoNetwork cardanoNetwork(@Value("${lob.cardano.network:main}") CardanoNetwork network) {
        log.info("Configured backend network:{}", network);

        return network;
    }

}
