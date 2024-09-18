package org.cardanofoundation.lob.app.blockchain_reader;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataTransactionDetailsService;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockchainReaderPublicApi {

    private final BlockchainDataChainTipService blockchainDataChainTipService;
    private final BlockchainDataTransactionDetailsService blockchainDataTransactionDetailsService;

    public Either<Problem, ChainTip> getChainTip() {
        return blockchainDataChainTipService.getChainTip();
    }

    public Either<Problem, Optional<OnChainTxDetails>> getTxDetails(String transactionHash) {
        return blockchainDataTransactionDetailsService.getTransactionDetails(transactionHash);
    }

}
