package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import java.util.Set;

public enum BlockchainPublishStatus {

    STORED,

    SUBMITTED, // submitted and setting in the mem pool for now

    VISIBLE_ON_CHAIN, // confirmed to be visible on-chain

    COMPLETED, // confirmed on-chain and e.g. 5 - 40 blocks passed

    ROLLBACKED, // signal to resubmit the transaction since it disappeared from on chain

    FINALIZED; // 2140

    public static Set<BlockchainPublishStatus> toDispatchStatuses() {
        return Set.of(STORED, ROLLBACKED);
    }

}
