package org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain;

import lombok.val;
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
    private CardanoFinalityScoreCalculator calculatorDevnet;

    @BeforeEach
    void setUp() {
        // Initialize the calculators with real CardanoFinalityProvider for MAIN and DEV networks
        val finalityProviderMainnet = new CardanoFinalityProvider(CardanoNetwork.MAIN);
        calculatorMainnet = new CardanoFinalityScoreCalculator(finalityProviderMainnet);

        val finalityProviderDevnet = new CardanoFinalityProvider(CardanoNetwork.DEV);
        calculatorDevnet = new CardanoFinalityScoreCalculator(finalityProviderDevnet);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 95, LOW",
            "100, 90, MEDIUM",
            "100, 50, HIGH",
            "100, 49, HIGH",
            "3000, 2000, ULTRA_HIGH",
            "2200, 0, FINAL",
            "2160, 0, FINAL",
            "3000, 1000, ULTRA_HIGH",
            "100, 99, VERY_LOW"
    })
    void calculateFinalityScore_shouldReturnCorrectScore_Mainnet(long chainTip,
                                                                 long txAbsoluteSlot,
                                                                 CardanoFinalityScore expectedScore) {
        val result = calculatorMainnet.calculateFinalityScore(chainTip, txAbsoluteSlot);

        assertThat(result).isEqualTo(expectedScore);
    }

    @ParameterizedTest
    @CsvSource({
            "121, 0, FINAL",
            "120, 0, FINAL",
            "119, 0, ULTRA_HIGH",
            "100, 0, ULTRA_HIGH",
            "99, 0, VERY_HIGH",
            "80, 0, VERY_HIGH",
            "79, 0, HIGH",
            "50, 0, HIGH",
            "49, 0, MEDIUM",
            "10, 0, MEDIUM",
            "9, 0, LOW",
            "5, 0, LOW",
            "4, 0, VERY_LOW"
    })
    void calculateFinalityScore_shouldReturnCorrectScore_Devnet(long chainTip,
                                                                long txAbsoluteSlot,
                                                                CardanoFinalityScore expectedScore) {
        val result = calculatorDevnet.calculateFinalityScore(chainTip, txAbsoluteSlot);

        assertThat(result).isEqualTo(expectedScore);
    }

    @Test
    void calculateFinalityScore_whenChainTipLessThanTxAbsoluteSlot_shouldReturnVeryLow_Mainnet() {
        val result = calculatorMainnet.calculateFinalityScore(50, 100);

        assertThat(result).isEqualTo(CardanoFinalityScore.VERY_LOW);
    }

    @Test
    void calculateFinalityScore_whenChainTipLessThanTxAbsoluteSlot_shouldReturnVeryLow_Devnet() {
        val result = calculatorDevnet.calculateFinalityScore(50, 100);

        assertThat(result).isEqualTo(CardanoFinalityScore.VERY_LOW);
    }

}
