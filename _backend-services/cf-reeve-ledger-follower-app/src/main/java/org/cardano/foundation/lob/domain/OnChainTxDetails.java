package org.cardano.foundation.lob.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class OnChainTxDetails {

    private String transactionHash;
    private String blockHash;
    private long absoluteSlot;
    private long slotConfirmations; // number of slots since the transaction was included in a block
    private FinalityScore finalityScore;
    private CardanoNetwork network;

}
