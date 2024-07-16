package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;

@Service
@Slf4j
public class BlockchainPublishStatusMapper {

    public LedgerDispatchStatus convert(Optional<BlockchainPublishStatus> blockchainPublishStatus,
                                        Optional<OnChainAssuranceLevel> assuranceLevelM) {
        return blockchainPublishStatus.map(status -> {
            return switch (status) {
                case STORED, ROLLBACKED -> LedgerDispatchStatus.MARK_DISPATCH;
                case VISIBLE_ON_CHAIN, SUBMITTED -> LedgerDispatchStatus.DISPATCHED;
                case COMPLETED -> assuranceLevelM.map(level -> {
                    if (level.ordinal() >= OnChainAssuranceLevel.HIGH.ordinal()) {
                        return LedgerDispatchStatus.COMPLETED;
                    }

                    return LedgerDispatchStatus.DISPATCHED;
                }).orElse(LedgerDispatchStatus.DISPATCHED);
                case FINALIZED -> LedgerDispatchStatus.FINALIZED;
            };
        }).orElse(NOT_DISPATCHED);
    }

    public BlockchainPublishStatus convert(LedgerDispatchStatus ledgerDispatchStatus) {
        val blockchainPublishStatusM = switch (ledgerDispatchStatus) {
            case NOT_DISPATCHED -> Optional.<BlockchainPublishStatus>empty();
            case MARK_DISPATCH -> Optional.of(BlockchainPublishStatus.STORED);
            case DISPATCHED -> Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN);
            case COMPLETED -> Optional.of(BlockchainPublishStatus.COMPLETED);
            case FINALIZED -> Optional.of(BlockchainPublishStatus.FINALIZED);
        };

        return blockchainPublishStatusM.orElse(BlockchainPublishStatus.STORED);
    }

}
