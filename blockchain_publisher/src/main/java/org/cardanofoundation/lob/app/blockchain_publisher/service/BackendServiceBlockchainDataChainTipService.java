package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.backend.api.BackendService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.ChainTip;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Slf4j
@RequiredArgsConstructor
public class BackendServiceBlockchainDataChainTipService implements BlockchainDataChainTipService {

    private final BackendService backendService;

    @Override
    @SneakyThrows
    public Either<Problem,ChainTip> latestChainTip() {
        var latestBlock = backendService.getBlockService().getLatestBlock();

        if (latestBlock.isSuccessful()) {
            var block = latestBlock.getValue();

            return Either.right(new ChainTip(block.getSlot(), block.getHash()));
        }

        return Either.left(Problem.builder()
                .withTitle("Unable to get chain tip via backend service")
                .withDetail(latestBlock.getResponse())
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .build());
    }

}