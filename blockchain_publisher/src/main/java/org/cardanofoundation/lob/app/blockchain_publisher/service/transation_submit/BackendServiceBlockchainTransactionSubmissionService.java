package org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit;

import com.bloxbean.cardano.client.backend.api.BackendService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
public class BackendServiceBlockchainTransactionSubmissionService implements BlockchainTransactionSubmissionService {

    private final BackendService backendService;

    @SneakyThrows
    @Override
    public String submitTransaction(byte[] txData) {
        val result = backendService.getTransactionService().submitTransaction(txData);

        if (result.isSuccessful()) {
            return result.getValue();
        }

        throw new RuntimeException("Transaction submission failed with error: " + result.getResponse());
    }

}
