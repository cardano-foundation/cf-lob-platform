package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import java.util.Optional;

import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;

public record OnChainStatus(BlockchainPublishStatus status, Optional<FinalityScore> finalityScore) {
}
