package org.cardano.foundation.lob.service.transaction_submit;

import io.vavr.control.Either;
import org.cardano.foundation.lob.domain.L1SubmissionData;

import java.util.concurrent.TimeoutException;

public interface TransactionSubmissionService {

    /**
     * Submit transaction and return transaction  hash.
     *
     * @param txData
     * @return transaction hash
     */
    Either<Exception, String> submitTransaction(byte[] txData);

    /**
     * Submits transaction and gets L1 confirmation data
     * @param txData
     * @return
     */
    Either<Exception, L1SubmissionData> submitTransactionWithConfirmation(byte[] txData) throws TimeoutException, InterruptedException;

}
