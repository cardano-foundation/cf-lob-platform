package org.cardanofoundation.lob.app.blockchain_reader;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import io.vavr.control.Either;
import org.zalando.problem.Problem;

import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.cardanofoundation.lob.app.blockchain_reader.domain.LOBOnChainTxStatusRequest;
import org.cardanofoundation.lob.app.blockchain_reader.domain.LOBOnChainTxStatusResponse;

@RequiredArgsConstructor
@Slf4j
public class BlockchainReaderPublicApi implements BlockchainReaderPublicApiIF {

    private final RestClient restClient;
    private final CardanoNetwork network;

    @Value("${lob.blockchain_reader.lob_follower_base_url:http://localhost:9090/api}")
    private String lobFollowerBaseUrl;

    @PostConstruct
    public void init() {
        log.info("BlockchainReaderPublicApi initialized with network: {}", network);
    }

    @Override
    public Either<Problem, ChainTip> getChainTip() {
        try {
            val chainTip = restClient.get()
                    .uri(STR."\{lobFollowerBaseUrl}/tip")
                    .retrieve()
                    .body(ChainTip.class);

            if (chainTip.getNetwork() != network) {
                val problem = Problem.builder()
                        .withTitle("NETWORK_MISMATCH")
                        .withStatus(BAD_REQUEST)
                        .withDetail(STR."Network mismatch: \{chainTip.getNetwork()} != \{network}")
                        .build();

                return Either.left(problem);
            }

            return Either.right(chainTip);
        } catch (RestClientResponseException ex) {
            val problem = Problem.builder()
                    .withTitle("CHAIN_TIP_ERROR")
                    .withStatus(BAD_REQUEST)
                    .withDetail(STR."Error from the client: \{ex.getResponseBodyAsString()}")
                    .build();
            return Either.left(problem);  // Return as Either.left
        } catch (RestClientException ex) {
            log.error("Error while fetching chain tip", ex);
            val problem = Problem.builder()
                    .withTitle("CHAIN_TIP_ERROR")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .withDetail(STR."Internal server error, reason: \{ex.getMessage()}")
                    .build();

            return Either.left(problem);
        }
    }

    @Override
    public Either<Problem, Optional<OnChainTxDetails>> getTxDetails(String transactionHash) {
        try {
            val txDetails = restClient.get()
                    .uri(STR."\{lobFollowerBaseUrl}/tx-details/\{transactionHash}")
                    .retrieve()
                    .body(OnChainTxDetails.class);

            if (txDetails.getNetwork() != network) {
                val problem = Problem.builder()
                        .withTitle("NETWORK_MISMATCH")
                        .withStatus(BAD_REQUEST)
                        .withDetail(STR."Network mismatch: \{txDetails.getNetwork()} != \{network}")
                        .build();

                return Either.left(problem);
            }

            return Either.right(Optional.of(txDetails));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Either.right(Optional.empty());
            }

            val problem = Problem.builder()
                    .withTitle("TX_DETAILS_ERROR")
                    .withStatus(BAD_REQUEST)
                    .withDetail(STR."Error from the client: \{ex.getResponseBodyAsString()}")
                    .build();
            return Either.left(problem);  // Return as Either.left
        } catch (RestClientException ex) {
            log.error("Error while fetching tx details", ex);

            val problem = Problem.builder()
                    .withTitle("TX_DETAILS_ERROR")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .withDetail(STR."Internal server error, reason: \{ex.getMessage()}")
                    .build();

            return Either.left(problem);
        }
    }

    @Override
    public Either<Problem, Map<String, Boolean>> isOnChain(Set<String> transactionIds) {
        try {
            val lobOnChainDetailsResponse = restClient.post()
                    .uri(STR."\{lobFollowerBaseUrl}/on-chain-statuses")
                    .body(new LOBOnChainTxStatusRequest(transactionIds))
                    .retrieve()
                    .body(LOBOnChainTxStatusResponse.class);

            if (lobOnChainDetailsResponse.getNetwork() != network) {
                val problem = Problem.builder()
                        .withTitle("NETWORK_MISMATCH")
                        .withStatus(BAD_REQUEST)
                        .withDetail(STR."Network mismatch: \{lobOnChainDetailsResponse.getNetwork()} != \{network}")
                        .build();

                return Either.left(problem);
            }

            return Either.right(lobOnChainDetailsResponse.getTransactionStatuses());
        } catch (RestClientResponseException ex) {
            val problem = Problem.builder()
                    .withTitle("LOB_TX_STATUSES_ERROR")
                    .withStatus(BAD_REQUEST)
                    .withDetail(STR."Error from the client: \{ex.getResponseBodyAsString()}")
                    .build();

            return Either.left(problem);  // Return as Either.left
        } catch (RestClientException ex) {
            log.error("Error while fetching on-chain statuses", ex);

            val problem = Problem.builder()
                    .withTitle("LOB_TX_STATUSES_ERROR")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .withDetail(STR."Internal server error, reason: \{ex.getMessage()}")
                    .build();

            return Either.left(problem);
        }
    }

}
