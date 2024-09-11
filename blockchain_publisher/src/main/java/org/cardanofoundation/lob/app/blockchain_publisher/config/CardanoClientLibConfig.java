package org.cardanofoundation.lob.app.blockchain_publisher.config;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CardanoClientLibConfig {

    @Bean
    public BackendService backendService(@Value("${lob.blockfrost.url}") String blockfrostUrl,
                                         @Value("${lob.blockfrost.api.key}") String blockfrostApiKey) {

        return new BFBackendService(blockfrostUrl, blockfrostApiKey);
    }

    @Bean
    @Qualifier("lob_owner_account")
    public Account ownerAccount(CardanoNetwork cardanoNetwork,
                                @Value("${lob_owner_account_mnemonic}" ) String lobOwnerMnemonics) {
        var organiserAccount = switch(cardanoNetwork) {
            case MAIN -> new Account(Networks.mainnet(), lobOwnerMnemonics);
            case PREPROD -> new Account(Networks.preprod(), lobOwnerMnemonics);
            case PREVIEW -> new Account(Networks.preview(), lobOwnerMnemonics);
            case DEV -> new Account(Networks.testnet(), lobOwnerMnemonics);
        };

        log.info("LOB's address:{}, stakeAddress:{}", organiserAccount.baseAddress(), organiserAccount.stakeAddress());

        return organiserAccount;
    }

}
