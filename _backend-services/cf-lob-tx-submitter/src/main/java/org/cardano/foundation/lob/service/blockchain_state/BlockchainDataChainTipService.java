package org.cardano.foundation.lob.service.blockchain_state;


import io.vavr.control.Either;
import org.cardano.foundation.lob.domain.ChainTip;

public interface BlockchainDataChainTipService {

    Either<Exception, ChainTip> getChainTip();

}
