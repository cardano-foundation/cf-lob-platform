package org.cardanofoundation.lob.app.blockchain_common.domain;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
public class OnChainTxDetails {

    private String transactionHash;
    private String blockHash;
    private long absoluteSlot;
    private long slotConfirmations; // number of slots since the transaction was included in a block
    private FinalityScore finalityScore;
    private CardanoNetwork network;

}
