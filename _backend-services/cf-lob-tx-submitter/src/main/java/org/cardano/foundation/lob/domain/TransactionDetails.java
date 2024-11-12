package org.cardano.foundation.lob.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TransactionDetails {
    private String transactionHash;
    private long absoluteSlot;
    private String blockHash;
}