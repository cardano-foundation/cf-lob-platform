package org.cardanofoundation.lob.app.blockchain_reader.service;

import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;

public interface SlotLengthProvider {

    long getSlotLength(CardanoNetwork network);

}
