package org.cardano.foundation.lob.config;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import lombok.extern.slf4j.Slf4j;
import org.cardano.foundation.lob.domain.CardanoNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CardanoClientLibConfig {

    @Bean
    public BackendService backendService(@Value("${blockfrost.url}") String blockfrostUrl,
                                         @Value("${blockfrost.api.key}") String blockfrostApiKey) {
        return new BFBackendService(blockfrostUrl, blockfrostApiKey);
    }

    @Bean
    public Network network(CardanoNetwork cardanoNetwork) {
        return switch(cardanoNetwork) {
            case MAIN -> Networks.mainnet();
            case PREPROD -> Networks.preprod();
            case PREVIEW -> Networks.preview();
            case DEV -> Networks.testnet();
        };
    }

    @Bean
    public Account organiserAccount(Network network,
                                    @Value("${organiser.account.mnemonic}" ) String organiserMnemonic) {
        var organiserAccount = new Account(network, organiserMnemonic);

        log.info("Organiser's address:{}, stakeAddress:{}, network:{}", organiserAccount.baseAddress(), organiserAccount.stakeAddress(), network);

        return organiserAccount;
    }

}
