package org.cardanofoundation.lob.app.blockchain_reader.config;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApi;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataTransactionDetailsService;
import org.cardanofoundation.lob.app.blockchain_reader.service.TransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PublicBlockchainReaderConfig {

    @Bean
    @ConditionalOnProperty(prefix = "lob.blockchain_reader", value = "enabled", havingValue = "true")
    public BlockchainReaderPublicApiIF blockchainReaderPublicApiReal(BlockchainDataChainTipService blockchainDataChainTipService,
                                                                     BlockchainDataTransactionDetailsService blockchainDataTransactionDetailsService,
                                                                     TransactionService transactionService) {
        log.info("Creating BlockchainReaderPublicApi with real YACI service, blockchain_reader enabled.");

        return new BlockchainReaderPublicApi(blockchainDataChainTipService, blockchainDataTransactionDetailsService, transactionService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "lob.blockchain_reader", value = "enabled", havingValue = "false")
    public BlockchainReaderPublicApiIF blockchainReaderPublicApiFake(CardanoNetwork network) {
        log.info("Creating BlockchainReaderPublicApi with fake YACI service, blockchain_reader disabled.");

        return new BlockchainReaderPublicApi.Noop(network);
    }

}
