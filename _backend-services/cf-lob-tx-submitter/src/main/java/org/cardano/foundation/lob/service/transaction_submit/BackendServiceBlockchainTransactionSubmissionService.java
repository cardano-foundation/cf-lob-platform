package org.cardano.foundation.lob.service.transaction_submit;

import com.bloxbean.cardano.client.backend.api.BackendService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardano.foundation.lob.service.blockchain_state.BlockchainTransactionSubmissionService;

@RequiredArgsConstructor
@Slf4j
public class BackendServiceBlockchainTransactionSubmissionService implements BlockchainTransactionSubmissionService {

    private final BackendService backendService;

    @Override
    public Either<Exception, String> submitTransaction(byte[] txData) {
        try {
            val result = backendService.getTransactionService().submitTransaction(txData);

            if (result.isSuccessful()) {
                return Either.right(result.getValue());
            }

            return Either.left(new RuntimeException("Unable to submit transaction via backend service, error: " + result.getResponse()));
        } catch (Exception e) {
            log.error("Error submitting transaction", e);

            return Either.left(e);
        }
    }

}
