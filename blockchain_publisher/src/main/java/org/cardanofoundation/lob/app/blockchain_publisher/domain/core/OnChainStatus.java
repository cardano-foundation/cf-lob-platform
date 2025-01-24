package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;

public record OnChainStatus(BlockchainPublishStatus status, FinalityScore finalityScore) {
}
