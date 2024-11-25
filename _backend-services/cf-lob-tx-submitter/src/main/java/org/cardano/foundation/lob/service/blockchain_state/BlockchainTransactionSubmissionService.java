package org.cardano.foundation.lob.service.blockchain_state;

import io.vavr.control.Either;

public interface BlockchainTransactionSubmissionService {

    /**
     * Submit transaction and return transaction  hash.
     *
     * @param txData
     * @return transaction hash
     */
    Either<Exception, String> submitTransaction(byte[] txData);

}
