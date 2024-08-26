package org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.backend.api.BackendService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoOnChainData;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackendServiceBlockchainTransactionDataProvider implements BlockchainTransactionDataProvider {

    private final BackendService backendService;

    @Override
    public Either<Problem, Optional<CardanoOnChainData>> getCardanoOnChainData(String transactionHash) {
        log.info("Fetching transaction data for transaction hash: {}", transactionHash);
        try {
            val transaction = backendService.getTransactionService().getTransaction(transactionHash);
            log.info("Transaction data fetched for transaction hash: {}, response:{}", transactionHash, transaction.getResponse());
            if (transaction.isSuccessful()) {
                log.info("Transaction data fetched successfully for transaction hash: {}", transactionHash);
                val txData = transaction.getValue();
                val absoluteSlot = txData.getSlot().longValue();

                return Either.right(Optional.of(new CardanoOnChainData(transactionHash, absoluteSlot)));
            }

            log.info("Transaction data not found for transaction hash: {}", transactionHash);

            return Either.right(Optional.empty());
        } catch (ApiException | RuntimeException ex) {
            return Either.left(Problem.builder()
                    .withTitle("TRANSACTION_LOOKUP_ERROR")
                    .withDetail(ex.getMessage())
                    .with("transactionHash", transactionHash)
                    .build()
            );
        }
    }

}
