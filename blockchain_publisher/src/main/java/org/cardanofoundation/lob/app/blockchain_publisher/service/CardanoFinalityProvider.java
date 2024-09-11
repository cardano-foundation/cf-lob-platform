package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardanoFinalityProvider {

    private final CardanoNetwork network;
    private final SlotLengthProvider slotLengthProvider;

    public CardanoFinalityScore getFinalityScore(long slots) {
        return switch (network) {
            case MAIN, PREPROD, PREVIEW:
                yield getMainnetFinalityScore(slots);
            case DEV:
                yield getDevnetFinalityScore(slots);
        };
    }

    private CardanoFinalityScore getMainnetFinalityScore(long slots) {
        if (slots >= 2160 * slotLengthProvider.getSlotLength(network)) { // ca. 12 hours
            return CardanoFinalityScore.FINAL;
        } else if (slots >= 250 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.ULTRA_HIGH;
        } else if (slots >= 100 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.VERY_HIGH;
        } else if (slots >= 30 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.HIGH;
        } else if (slots >= 15 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.MEDIUM;
        } else if (slots >= 5 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.LOW;
        }

        return CardanoFinalityScore.VERY_LOW;
    }

    private CardanoFinalityScore getDevnetFinalityScore(long slots) {
        if (slots >= 12 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.FINAL;
        } else if (slots >= 11 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.ULTRA_HIGH;
        } else if (slots >= 10 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.VERY_HIGH;
        } else if (slots >= 5 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.HIGH;
        } else if (slots >= 2 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.MEDIUM;
        } else if (slots >= 1 * slotLengthProvider.getSlotLength(network)) {
            return CardanoFinalityScore.LOW;
        }

        return CardanoFinalityScore.VERY_LOW;
    }

}
