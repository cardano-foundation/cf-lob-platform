package org.cardanofoundation.lob.app.blockchain_publisher.service;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;

public interface SlotLengthProvider {

    long getSlotLength(CardanoNetwork network);

}
