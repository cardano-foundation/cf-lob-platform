package org.cardanofoundation.lob.app.blockchain_reader.service;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.zalando.problem.Problem;

import java.util.Optional;

public interface BlockchainDataTransactionDetailsService {

    default Either<Problem, Optional<OnChainTxDetails>> getTransactionDetails(String transactionHash) {
        return Either.right(Optional.empty());
    }

}
