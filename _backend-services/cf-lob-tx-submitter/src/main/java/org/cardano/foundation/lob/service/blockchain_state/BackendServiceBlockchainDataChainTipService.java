package org.cardano.foundation.lob.service.blockchain_state;

import com.bloxbean.cardano.client.backend.api.BackendService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardano.foundation.lob.domain.ChainTip;

@Slf4j
@RequiredArgsConstructor
public class BackendServiceBlockchainDataChainTipService implements BlockchainDataChainTipService {

    private final BackendService backendService;

    @Override
    public Either<Exception, ChainTip> getChainTip() {
        try {
            val latestBlock = backendService.getBlockService().getLatestBlock();

            if (latestBlock.isSuccessful()) {
                val block = latestBlock.getValue();

                return Either.right(new ChainTip(block.getSlot(), block.getHash()));
            }

            return Either.left(new RuntimeException("Unable to get chain tip via backend service"));
        } catch (Exception e) {
            log.error("Error getting chain tip", e);

            return Either.left(e);
        }
    }

}
