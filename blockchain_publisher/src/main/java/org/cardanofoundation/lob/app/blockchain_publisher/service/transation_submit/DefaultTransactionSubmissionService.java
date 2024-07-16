package org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.backend.api.BackendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.L1Submission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultTransactionSubmissionService implements TransactionSubmissionService {

    private final BlockchainTransactionSubmissionService transactionSubmissionService;

    private final BackendService backendService;

    private final Clock clock;

    @Value("${lob.transaction.submission.sleep.seconds:5}")
    private int sleepTimeSeconds;

    @Value("${lob.transaction.submission.timeout.in.minutes:5}")
    private int timeoutInMinutes;

    @Override
    public String submitTransaction(byte[] txData) {
        return transactionSubmissionService.submitTransaction(txData);
    }

    @Override
    public L1Submission submitTransactionWithConfirmation(byte[] txData) throws TimeoutException, InterruptedException, ApiException {
        val txHash = submitTransaction(txData);

        val start = LocalDateTime.now(clock);

        val future = start.plusMinutes(timeoutInMinutes);

        while (LocalDateTime.now(clock).isBefore(future)) {
            val transactionDetailsR = backendService.getTransactionService().getTransaction(txHash);

            if (!transactionDetailsR.isSuccessful()) {
                log.warn("Transaction not found on chain yet. Sleeping for {} seconds... until deadline:{}", sleepTimeSeconds, future);
                Thread.sleep(sleepTimeSeconds * 1000L);
                continue;
            }

            val transactionContent = transactionDetailsR.getValue();

            return new L1Submission(transactionContent.getHash(), transactionContent.getSlot());
        }

        throw new TimeoutException(STR."Transaction with txHash: \{txHash} not confirmed within timeout!");
    }
}