package org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.service.CardanoFinalityProvider;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardanoFinalityScoreCalculator {

    private final CardanoFinalityProvider cardanoFinalityProvider;

    public CardanoFinalityScore calculateFinalityScore(long chainTip,
                                                       long txAbsoluteSlot) {
        val slotsDiff = (chainTip - txAbsoluteSlot);

        return cardanoFinalityProvider.getFinalityScore(slotsDiff);
    }

}
