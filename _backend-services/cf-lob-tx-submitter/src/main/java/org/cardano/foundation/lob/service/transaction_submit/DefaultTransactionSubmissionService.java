package org.cardano.foundation.lob.service.transaction_submit;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardano.foundation.lob.domain.L1SubmissionData;
import org.cardano.foundation.lob.service.blockchain_state.BlockchainDataTransactionDetailsService;
import org.cardano.foundation.lob.service.blockchain_state.BlockchainTransactionSubmissionService;
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
    private final BlockchainDataTransactionDetailsService blockchainDataTransactionDetailsService;
    private final Clock clock;

    @Value("${transaction.submission.timeout.minutes:15}")
    private int timeoutInMinutes;

    @Value("${transaction.submission.sleep.seconds:5}")
    private int sleepTimeInSeconds;

    @Override
    public Either<Exception, String> submitTransaction(byte[] txData) {
        return transactionSubmissionService.submitTransaction(txData);
    }

    @Override
    @SneakyThrows
    public Either<Exception, L1SubmissionData> submitTransactionWithConfirmation(byte[] txData) throws InterruptedException {
        val txHashE = submitTransaction(txData);
        if (txHashE.isLeft()) {
            return Either.left(txHashE.getLeft());
        }

        val txHash = txHashE.get();

        val start = LocalDateTime.now(clock);

        val future = start.plusMinutes(timeoutInMinutes);

        while (LocalDateTime.now(clock).isBefore(future)) {
            val transactionDetailsE = blockchainDataTransactionDetailsService.getTransactionDetails(txHash);

            if (transactionDetailsE.isRight()) {
                var transactionDetails = transactionDetailsE.get();
                if (transactionDetails.isPresent()) {

                    val trxDetails = transactionDetails.get();
                    return Either.right(L1SubmissionData.builder()
                            .txHash(trxDetails.getTransactionHash())
                            .absoluteSlot(trxDetails.getAbsoluteSlot())
                            .build());
                }
            }

            log.info("Transaction not confirmed yet. Sleeping for {} seconds... until deadline:{}", sleepTimeInSeconds, future);

            Thread.sleep(sleepTimeInSeconds * 1000L);
        }

        return Either.left(new TimeoutException("Transaction not confirmed within timeout period!"));
    }

}
