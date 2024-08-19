package org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoOnChainData;
import org.zalando.problem.Problem;

import java.util.Optional;

public interface BlockchainTransactionDataProvider {

    Either<Problem, Optional<CardanoOnChainData>> getCardanoOnChainData(String transactionHash);

}
