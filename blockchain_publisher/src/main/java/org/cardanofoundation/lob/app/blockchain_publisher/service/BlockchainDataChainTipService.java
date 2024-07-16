package org.cardanofoundation.lob.app.blockchain_publisher.service;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.ChainTip;
import org.zalando.problem.Problem;

public interface BlockchainDataChainTipService {

    Either<Problem, ChainTip> latestChainTip();

}
