package org.cardanofoundation.lob.app.blockchain_common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ChainTip {

    private long absoluteSlot;

    private String blockHash;

    @Builder.Default
    private Optional<Integer> epochNo = Optional.empty();

    private CardanoNetwork network;

    private boolean isSynced;

}
