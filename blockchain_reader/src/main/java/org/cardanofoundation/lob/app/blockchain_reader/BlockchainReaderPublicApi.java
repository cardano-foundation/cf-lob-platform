package org.cardanofoundation.lob.app.blockchain_reader;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_reader.service.BlockchainDataTransactionDetailsService;
import org.cardanofoundation.lob.app.blockchain_reader.service.TransactionService;
import org.zalando.problem.Problem;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BlockchainReaderPublicApi implements BlockchainReaderPublicApiIF {

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

    @RequiredArgsConstructor
    public static class Noop implements BlockchainReaderPublicApiIF {

        private final CardanoNetwork network;

        public Either<Problem, ChainTip> getChainTip() {
            return Either.right(ChainTip.builder()
                    .absoluteSlot(1)
                    .blockHash("0db89ee763a3f9fdb3f5b6e0e0d21b2da8d5768034d9090ac2d011b94bf0f9ef")
                    .epochNo(Optional.of(1))
                    .network(network)
                    .isSynced(true)
                    .build()
            );
        }

        public Either<Problem, Optional<OnChainTxDetails>> getTxDetails(String transactionHash) {
            return Either.right(Optional.of(OnChainTxDetails.builder()
                    .transactionHash(transactionHash)
                    .blockHash("0db89ee763a3f9fdb3f5b6e0e0d21b2da8d5768034d9090ac2d011b94bf0f9ef")
                    .absoluteSlot(1)
                    .slotConfirmations(1_000_000)
                    .network(network)
                    .finalityScore(FinalityScore.FINAL)
                    .build())
            );
        }

    }

}
