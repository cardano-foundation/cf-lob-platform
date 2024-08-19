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

    private final CardanoNetwork cardanoNetwork;

    public CardanoFinalityScore getFinalityScore(long slots) {
        return switch (cardanoNetwork) {
            case MAIN, PREPROD, PREVIEW:
                yield getMainnetFinalityScore(slots);
            case DEV:
                yield getDevnetFinalityScore(slots);
        };
    }

    private CardanoFinalityScore getMainnetFinalityScore(long slots) {
        if (slots >= 2160) {
            return CardanoFinalityScore.FINAL;
        } else if (slots >= 1000) {
            return CardanoFinalityScore.ULTRA_HIGH;
        } else if (slots >= 250) {
            return CardanoFinalityScore.VERY_HIGH;
        } else if (slots >= 50) {
            return CardanoFinalityScore.HIGH;
        } else if (slots >= 10) {
            return CardanoFinalityScore.MEDIUM;
        } else if (slots >= 5) {
            return CardanoFinalityScore.LOW;
        }

        return CardanoFinalityScore.VERY_LOW;
    }

    private CardanoFinalityScore getDevnetFinalityScore(long slots) {
        if (slots >= 120) {
            return CardanoFinalityScore.FINAL;
        } else if (slots >= 100) {
            return CardanoFinalityScore.ULTRA_HIGH;
        } else if (slots >= 80) {
            return CardanoFinalityScore.VERY_HIGH;
        } else if (slots >= 50) {
            return CardanoFinalityScore.HIGH;
        } else if (slots >= 10) {
            return CardanoFinalityScore.MEDIUM;
        } else if (slots >= 5) {
            return CardanoFinalityScore.LOW;
        }

        return CardanoFinalityScore.VERY_LOW;
    }

}
