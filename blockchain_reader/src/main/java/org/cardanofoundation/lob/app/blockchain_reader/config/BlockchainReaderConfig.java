package org.cardanofoundation.lob.app.blockchain_reader.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.yaci.store.api.blocks.service.BlockService;
import com.bloxbean.cardano.yaci.store.api.transaction.service.TransactionService;
import com.bloxbean.cardano.yaci.store.core.service.EraService;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_reader.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConditionalOnProperty(name = "lob.blockchain_reader.enabled", havingValue = "true", matchIfMissing = true)
public class BlockchainReaderConfig {

    @Bean
    public BlockchainDataChainTipService blockchainDataChainTipService(CardanoNetwork network,
                                                                       BlockService blockService,
                                                                       ChainSyncService chainSyncService,
                                                                       EraService eraService,
                                                                       CacheManager cacheManager) {
        return new YaciChainTipService(blockService, chainSyncService, eraService, network, cacheManager);
    }

    @Bean
    @Profile( value = { "dev--yaci-dev-kit", "test" })
    public ChainSyncService dummyChainSyncService() {
        return new ChainSyncService.Noop();
    }

    @Bean
    @Profile( value = { "prod", "dev--preprod"} )
    public ChainSyncService defaultChainSyncService(@Qualifier("yaci_blockfrost") BackendService yaciBackendService,
                                                    @Qualifier("original_blockfrost") BackendService orgBackendService,
                                                    @Value("${lob.blochain_reader.chain.sync.buffer:30}") int chainSyncBuffer) {
        return new DefaultChainSyncService(orgBackendService, yaciBackendService, chainSyncBuffer);
    }

    @Bean
    public BlockchainDataTransactionDetailsService blockchainDataTransactionDetailsService(CardanoNetwork network,
                                                                                           BlockService blockService,
                                                                                           TransactionService transactionService,
                                                                                           FinalityScoreCalculator finalityScoreCalculator,
                                                                                           CacheManager cacheManager
                                                                                           ) {
        return new YaciTransactionDetailsBlockchainDataService(blockService, transactionService, network, finalityScoreCalculator, cacheManager);
    }

}
