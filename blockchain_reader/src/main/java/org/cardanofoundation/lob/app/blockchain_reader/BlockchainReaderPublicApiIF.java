package org.cardanofoundation.lob.app.blockchain_reader;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.zalando.problem.Problem;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BlockchainReaderPublicApiIF {

    Either<Problem, ChainTip> getChainTip();

    Either<Problem, Optional<OnChainTxDetails>> getTxDetails(String transactionHash);

    Either<Problem, Map<String, Boolean>> isOnChain(Set<String> transactionIds);

}
