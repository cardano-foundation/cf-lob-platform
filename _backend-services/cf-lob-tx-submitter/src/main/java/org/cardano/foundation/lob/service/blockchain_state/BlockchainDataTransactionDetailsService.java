package org.cardano.foundation.lob.service.blockchain_state;

import io.vavr.control.Either;
import org.cardano.foundation.lob.domain.TransactionDetails;

import java.util.Optional;

public interface BlockchainDataTransactionDetailsService {

    default Either<Exception, Optional<TransactionDetails>> getTransactionDetails(String transactionHash) {
        return Either.right(Optional.empty());
    }

}
