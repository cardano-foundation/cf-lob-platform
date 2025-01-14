package org.cardanofoundation.lob.app.blockchain_publisher.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.common.model.Networks;

import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;

@Configuration("blockchain_publisher.cardano_client_lib")
@Slf4j
public class CardanoClientLibConfig {

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
