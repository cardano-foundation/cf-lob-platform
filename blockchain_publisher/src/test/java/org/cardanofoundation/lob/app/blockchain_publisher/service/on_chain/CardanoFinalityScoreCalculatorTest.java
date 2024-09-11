package org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain;

import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.StaticSlotLengthProvider;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_publisher.service.CardanoFinalityProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardanoFinalityScoreCalculatorTest {

    private CardanoFinalityScoreCalculator calculatorMainnet;

    @BeforeEach
    void setUp() {
        // Initialize the calculators with real CardanoFinalityProvider for MAIN and DEV networks
        val slotLengthProviderMainnet = new StaticSlotLengthProvider(20, 1);
        val finalityProviderMainnet = new CardanoFinalityProvider(CardanoNetwork.MAIN, slotLengthProviderMainnet);
        calculatorMainnet = new CardanoFinalityScoreCalculator(finalityProviderMainnet);
    }

    @ParameterizedTest
    @CsvSource({
            "2000, 1900, LOW",
            "2000, 1800, LOW",
            "2000, 1000, HIGH",
            "2000, 980 , HIGH",
            "60000, 20000, ULTRA_HIGH",
            "44000, 0, FINAL",
            "43200, 0, FINAL",
            "60000, 20000, ULTRA_HIGH",
            "2000, 1980, VERY_LOW",
            "2000, 1820, LOW",
            "2000, 1700, MEDIUM"
    })
    void calculateFinalityScore_shouldReturnCorrectScore_Mainnet(long chainTip,
                                                                 long txAbsoluteSlot,
                                                                 CardanoFinalityScore expectedScore) {
        val result = calculatorMainnet.calculateFinalityScore(chainTip, txAbsoluteSlot);

        assertThat(result).isEqualTo(expectedScore);
    }

    @Test
    void calculateFinalityScore_whenChainTipLessThanTxAbsoluteSlot_shouldReturnVeryLow_Mainnet() {
        val result = calculatorMainnet.calculateFinalityScore(20_000, 19_999);

        assertThat(result).isEqualTo(CardanoFinalityScore.VERY_LOW);
    }

}
