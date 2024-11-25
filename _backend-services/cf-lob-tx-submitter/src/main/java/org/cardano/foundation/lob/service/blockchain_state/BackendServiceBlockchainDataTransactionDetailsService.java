package org.cardano.foundation.lob.service.blockchain_state;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.backend.api.BackendService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardano.foundation.lob.domain.TransactionDetails;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BackendServiceBlockchainDataTransactionDetailsService implements BlockchainDataTransactionDetailsService {

    private final BackendService backendService;

    @Override
    public Either<Exception, Optional<TransactionDetails>> getTransactionDetails(String transactionHash) {
        try {
            val result = backendService.getTransactionService().getTransaction(transactionHash);

            if (result.isSuccessful()) {
                val trx = result.getValue();

                return Either.right(Optional.of(TransactionDetails.builder()
                        .transactionHash(trx.getHash())
                        .absoluteSlot(trx.getSlot())
                        .blockHash(trx.getBlock())
                        .build())
                );
            }
        } catch (ApiException e) {
            log.error("Error getting transaction details for hash: {}", transactionHash, e);

            return Either.left(e);
        }

        return Either.left(new RuntimeException("Unable to get transaction details via backend service"));
    }

}
