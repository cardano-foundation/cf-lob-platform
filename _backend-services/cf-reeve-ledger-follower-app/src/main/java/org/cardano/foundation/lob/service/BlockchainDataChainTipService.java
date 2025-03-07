package org.cardano.foundation.lob.service;

import io.vavr.control.Either;
import org.cardano.foundation.lob.domain.ChainTip;
import org.zalando.problem.Problem;

public interface BlockchainDataChainTipService {

    Either<Problem, ChainTip> getChainTip();

}
