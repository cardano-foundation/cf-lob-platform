package org.cardanofoundation.lob.app.blockchain_reader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardanoFinalityProvider {

    @Qualifier("blockchain_reader.network")
    private final CardanoNetwork network;
    private final SlotLengthProvider slotLengthProvider;

    public FinalityScore getFinalityScore(long slots) {
        return switch (network) {
            case MAIN, PREPROD, PREVIEW:
                yield getMainnetFinalityScore(slots);
            case DEV:
                yield getDevnetFinalityScore(slots);
        };
    }

    private FinalityScore getMainnetFinalityScore(long slotsDiff) {
        if (slotsDiff >= 2160 * slotLengthProvider.getSlotLength(network)) { // ca. 12 hours
            return FinalityScore.FINAL;
        } else if (slotsDiff >= 250 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.ULTRA_HIGH;
        } else if (slotsDiff >= 100 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.VERY_HIGH;
        } else if (slotsDiff >= 30 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.HIGH;
        } else if (slotsDiff >= 15 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.MEDIUM;
        } else if (slotsDiff >= 5 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.LOW;
        }

        return FinalityScore.VERY_LOW;
    }

    private FinalityScore getDevnetFinalityScore(long slots) {
        if (slots >= 150 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.FINAL;
        } else if (slots >= 100 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.ULTRA_HIGH;
        } else if (slots >= 80 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.VERY_HIGH;
        } else if (slots >= 60 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.HIGH;
        } else if (slots >= 40 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.MEDIUM;
        } else if (slots >= 20 * slotLengthProvider.getSlotLength(network)) {
            return FinalityScore.LOW;
        }

        return FinalityScore.VERY_LOW;
    }

}
