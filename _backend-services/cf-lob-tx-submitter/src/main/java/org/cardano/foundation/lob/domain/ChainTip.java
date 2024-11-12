package org.cardano.foundation.lob.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChainTip {
    private long absoluteSlot;
    private String hash;
}
