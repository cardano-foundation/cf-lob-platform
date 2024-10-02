package org.cardano.foundation.lob.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cardano.foundation.lob.domain.CardanoNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StaticSlotLengthProvider implements SlotLengthProvider {

    @Value("${lob.cardano.main.slot.length:20}")
    @Getter
    @Setter
    private int mainnetSlotLength;

    @Value("${lob.cardano.dev.slot.length:1}")
    @Getter
    @Setter
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
