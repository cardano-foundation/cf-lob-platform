package org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.L1Submission;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DefaultTransactionSubmissionService implements TransactionSubmissionService {

    private final BlockchainTransactionSubmissionService transactionSubmissionService;

    private final BackendService backendService;

    private final Clock clock;

    private final int sleepTimeSeconds;

    private final int timeoutInSeconds;

    @Override
    public String submitTransaction(byte[] txData) {
        log.info("Submitting transaction without confirmation.., txId:{}", TransactionUtil.getTxHash(txData));

        return transactionSubmissionService.submitTransaction(txData);
    }

    @Override
    public L1Submission submitTransactionWithPossibleConfirmation(byte[] txData) throws InterruptedException, ApiException {
        log.info("Submitting transaction with confirmation.., txId:{}", TransactionUtil.getTxHash(txData));
        val txHash = submitTransaction(txData);

        val start = LocalDateTime.now(clock);
        val future = start.plusSeconds(timeoutInSeconds);

        while (LocalDateTime.now(clock).isBefore(future)) {
            val transactionDetailsR = backendService.getTransactionService().getTransaction(txHash);

            if (!transactionDetailsR.isSuccessful()) {
                log.warn("Transaction not found on chain yet. Sleeping for {} seconds... until deadline:{}", sleepTimeSeconds, future);
                Thread.sleep(sleepTimeSeconds * 1000L);
                continue;
            }

            val transactionContent = transactionDetailsR.getValue();
            val absoluteSlot = transactionContent.getSlot();

            return new L1Submission(txHash, Optional.of(absoluteSlot), true);
        }

        return new L1Submission(txHash, Optional.empty(), false);
    }

}