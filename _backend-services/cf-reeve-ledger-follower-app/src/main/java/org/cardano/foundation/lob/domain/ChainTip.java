package org.cardano.foundation.lob.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    private String blockHash;

    @Builder.Default
    private Optional<Integer> epochNo = Optional.empty();

    @NotNull
    private CardanoNetwork network;

    private boolean isSynced;

}
