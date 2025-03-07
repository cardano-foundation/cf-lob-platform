package org.cardano.foundation.lob.service;

import org.cardano.foundation.lob.domain.CardanoNetwork;

public interface SlotLengthProvider {

    long getSlotLength(CardanoNetwork network);

}
