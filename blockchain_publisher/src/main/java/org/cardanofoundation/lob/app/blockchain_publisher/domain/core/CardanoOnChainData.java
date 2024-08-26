package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CardanoOnChainData {

    private String transactionHash;
    private Long absoluteSlot;

}
