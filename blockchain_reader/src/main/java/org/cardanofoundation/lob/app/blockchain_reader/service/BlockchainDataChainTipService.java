package org.cardanofoundation.lob.app.blockchain_reader.service;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.zalando.problem.Problem;

public interface BlockchainDataChainTipService {

    Either<Problem, ChainTip> getChainTip();

}
