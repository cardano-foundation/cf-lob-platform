package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;

import java.util.Optional;

public record OnChainStatus(BlockchainPublishStatus status, Optional<FinalityScore> finalityScore) {
}
