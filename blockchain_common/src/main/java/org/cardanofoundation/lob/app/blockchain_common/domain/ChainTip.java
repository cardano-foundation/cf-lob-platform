package org.cardanofoundation.lob.app.blockchain_common.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ChainTip {

    private long absoluteSlot;

    private String blockHash;

    @Builder.Default
    private Optional<Integer> epochNo = Optional.empty();

    @NotNull
    private CardanoNetwork network;

    private boolean isSynced;

}
