package org.cardanofoundation.lob.app.blockchain_publisher.config;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BackendService;
import org.cardanofoundation.lob.app.blockchain_common.service_assistance.MetadataChecker;
import org.cardanofoundation.lob.app.blockchain_publisher.service.L1TransactionCreator;
import org.cardanofoundation.lob.app.blockchain_publisher.service.API1MetadataSerialiser;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.*;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.http.HttpClient;
import java.time.Clock;

@Configuration
public class TransactionSubmissionConfig {

    @Bean
    @Profile(value = { "dev--yaci-dev-kit", "test"} )
    public BlockchainTransactionSubmissionService backendServiceTransactionSubmissionService(
            @Qualifier("yaci_blockfrost") BackendService backendService) {
        return new BackendServiceBlockchainTransactionSubmissionService(backendService);
    }

    @Bean
    public TransactionSubmissionService transactionSubmissionService(
            BlockchainTransactionSubmissionService trxSubmissionService,
            @Qualifier("yaci_blockfrost") BackendService backendService,
            Clock clock,
            @Value("${lob.transaction.submission.sleep.seconds:5}") int sleepTimeSeconds,
            @Value("${lob.transaction.submission.timeout.in.seconds:300}") int timeoutInSeconds
    ) {
        return new DefaultTransactionSubmissionService(trxSubmissionService, backendService, clock, sleepTimeSeconds, timeoutInSeconds);
    }

    @Bean
    public L1TransactionCreator l1TransactionCreator(@Qualifier("yaci_blockfrost") BackendService backendService,
                                                     API1MetadataSerialiser API1MetadataSerialiser,
                                                     BlockchainReaderPublicApiIF blockchainReaderPublicApi,
                                                     MetadataChecker metadataChecker,
                                                     Account organiserAccount,
                                                     @Value("${l1.transaction.metadata_label:1447}") int metadataLabel,
                                                     @Value("${l1.transaction.debug_store_output_tx:false}") boolean debugStoreOutputTx
                                                     ) {
        return new L1TransactionCreator(backendService,
                API1MetadataSerialiser,
                blockchainReaderPublicApi,
                metadataChecker,
                organiserAccount,
                metadataLabel,
                debugStoreOutputTx
        );
    }

//    @Bean
//    @Profile("dev--preprod")
//    public BlockchainTransactionSubmissionService noopCardanoSummitTransactionSubmissionService() {
//        return new BlockchainTransactionSubmissionService.Noop();
//    }

    @Bean
    @Profile( value = { "prod", "dev--preprod" } )
    public BlockchainTransactionSubmissionService cardanoSummitTransactionSubmissionService(HttpClient httpClient,
                                                                                            @Value("${lob.blockchain_publisher.tx.submit.url}") String cardanoSubmitApiUrl,
                                                                                            @Value("${lob.blockchain_publisher.tx.submit.timeout.in.seconds}") int timeoutInSeconds,
                                                                                            @Value("${lob.blockchain_publisher.tx.submit.api_key}") String apiKey) {
        return new CardanoSubmitApiBlockchainTransactionSubmissionService(cardanoSubmitApiUrl, apiKey, httpClient, timeoutInSeconds);
    }

}
