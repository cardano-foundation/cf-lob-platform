package org.cardanofoundation.lob.app.blockchain_publisher.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.BackendServiceBlockchainTransactionSubmissionService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.BlockchainTransactionSubmissionService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.CardanoSubmitApiBlockchainTransactionSubmissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.http.HttpClient;

@Configuration
public class TransactionSubmissionConfig {

    @Bean
    @Profile(value = { "dev--yaci-dev-kit", "test"} )
    public BlockchainTransactionSubmissionService backendServiceTransactionSubmissionService(BackendService backendService) {
        return new BackendServiceBlockchainTransactionSubmissionService(backendService);
    }

//    @Bean
//    @Profile("dev--preprod")
//    public BlockchainTransactionSubmissionService noopCardanoSummitTransactionSubmissionService() {
//        return new BlockchainTransactionSubmissionService.Noop();
//    }

    @Bean
    @Profile( value = { "prod", "dev--preprod" } )
    public BlockchainTransactionSubmissionService cardanoSummitTransactionSubmissionService(HttpClient httpClient,
                                                                                            @Value("${cardano.tx.submit.api.url}") String cardanoSubmitApiUrl) {
        return new CardanoSubmitApiBlockchainTransactionSubmissionService(cardanoSubmitApiUrl, httpClient);
    }

}
