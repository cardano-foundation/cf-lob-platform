package org.cardanofoundation.lob.app.blockchain_publisher;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_publisher.service.SlotLengthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class StaticSlotLengthProvider implements SlotLengthProvider {

    @Value("${lob.cardano.main.slot.length:20}")
    private int mainnetSlotLength;

    @Value("${lob.cardano.dev.slot.length:1}")
    private int devnetSlotLength;

    @Override
    @Cacheable("slotLength")
    public long getSlotLength(CardanoNetwork network) {
        return switch (network) {
            case MAIN, PREPROD, PREVIEW -> mainnetSlotLength;
            case DEV -> devnetSlotLength;
        };
    }

}
