package org.cardanofoundation.lob.app.blockchain_reader;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataTransactionDetailsService;
import org.cardanofoundation.lob.app.blockchain_reader.service.TransactionService;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockchainReaderPublicApi {

    private final BlockchainDataChainTipService blockchainDataChainTipService;
    private final BlockchainDataTransactionDetailsService blockchainDataTransactionDetailsService;
    private final TransactionService transactionService;

    public Either<Problem, ChainTip> getChainTip() {
        return blockchainDataChainTipService.getChainTip();
    }

    public Either<Problem, Optional<OnChainTxDetails>> getTxDetails(String transactionHash) {
        return blockchainDataTransactionDetailsService.getTransactionDetails(transactionHash);
    }

    public Either<Problem, Map<String, Boolean>> isOnChain(Set<String> transactionIds) {
        val result = transactionIds.stream()
                .collect(Collectors.toMap(
                        transactionId -> transactionId,
                        transactionService::exists
                ));

        return Either.right(result);
    }

}
