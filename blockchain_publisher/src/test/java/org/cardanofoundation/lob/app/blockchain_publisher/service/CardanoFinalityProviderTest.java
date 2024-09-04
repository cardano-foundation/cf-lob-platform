package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.StaticSlotLengthProvider;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoNetwork;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardanoFinalityProviderTest {

    private CardanoFinalityProvider cardanoFinalityProvider;

    @ParameterizedTest
    @CsvSource({
            "MAIN, 0, VERY_LOW",
            "MAIN, 80, VERY_LOW",
            "MAIN, 100, LOW",
            "MAIN, 180, LOW",
            "MAIN, 280, LOW",
            "MAIN, 300, MEDIUM",
            "MAIN, 580, MEDIUM",
            "MAIN, 360, MEDIUM",
            "MAIN, 1980, HIGH",
            "MAIN, 2000, VERY_HIGH",
            "MAIN, 43180, ULTRA_HIGH",
            "MAIN, 43200, FINAL"
    })
    void getFinalityScore_shouldReturnCorrectScoreBasedOnNetworkAndSlots(String networkStr,
                                                                         long slots,
                                                                         CardanoFinalityScore expectedScore) {
        // Arrange
        val network = CardanoNetwork.valueOf(networkStr);
        val slotLengthProvider = new StaticSlotLengthProvider(20, 1);
        cardanoFinalityProvider = new CardanoFinalityProvider(network, slotLengthProvider);

        // Act
        val result = cardanoFinalityProvider.getFinalityScore(slots);

        // Assert
        assertThat(result).isEqualTo(expectedScore);
    }

    @Test
    void getFinalityScore_shouldHandleEdgeCasesCorrectly() {
        // Arrange for MAINNET
        val slotLengthProvider = new StaticSlotLengthProvider(20, 1);
        cardanoFinalityProvider = new CardanoFinalityProvider(CardanoNetwork.MAIN, slotLengthProvider);

        // Act & Assert
        assertThat(cardanoFinalityProvider.getFinalityScore(0)).isEqualTo(CardanoFinalityScore.VERY_LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(4 * 20)).isEqualTo(CardanoFinalityScore.VERY_LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(5 * 20)).isEqualTo(CardanoFinalityScore.LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(10 * 20)).isEqualTo(CardanoFinalityScore.LOW); // Updated expectation
        assertThat(cardanoFinalityProvider.getFinalityScore(15 * 20)).isEqualTo(CardanoFinalityScore.MEDIUM); // Updated threshold
        assertThat(cardanoFinalityProvider.getFinalityScore(30 * 20)).isEqualTo(CardanoFinalityScore.HIGH);
        assertThat(cardanoFinalityProvider.getFinalityScore(100 * 20)).isEqualTo(CardanoFinalityScore.VERY_HIGH);
        assertThat(cardanoFinalityProvider.getFinalityScore(250 * 20)).isEqualTo(CardanoFinalityScore.ULTRA_HIGH);
        assertThat(cardanoFinalityProvider.getFinalityScore(2160 * 20)).isEqualTo(CardanoFinalityScore.FINAL);
   }

    @Test
    void getFinalityScore_shouldReturnFinalForExtremeLargeSlotValues() {
        // Arrange for MAINNET
        val slotLengthProvider = new StaticSlotLengthProvider(20, 1);
        cardanoFinalityProvider = new CardanoFinalityProvider(CardanoNetwork.MAIN, slotLengthProvider);

        // Act & Assert
        assertThat(cardanoFinalityProvider.getFinalityScore(Long.MAX_VALUE)).isEqualTo(CardanoFinalityScore.FINAL);
    }

}
