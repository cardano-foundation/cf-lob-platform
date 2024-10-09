package org.cardano.foundation.lob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardano.foundation.lob.domain.FinalityScore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinalityScoreCalculator {

    private final CardanoFinalityProvider cardanoFinalityProvider;

    public FinalityScore calculateFinalityScore(long chainTip,
                                                long txAbsoluteSlot) {
        val slotsDiff = (chainTip - txAbsoluteSlot);

        return cardanoFinalityProvider.getFinalityScore(slotsDiff);
    }

}
