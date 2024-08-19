package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.val;
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
            "MAIN, 4, VERY_LOW",
            "MAIN, 5, LOW",
            "MAIN, 9, LOW",
            "MAIN, 10, MEDIUM",
            "MAIN, 49, MEDIUM",
            "MAIN, 50, HIGH",
            "MAIN, 249, HIGH",
            "MAIN, 250, VERY_HIGH",
            "MAIN, 999, VERY_HIGH",
            "MAIN, 1000, ULTRA_HIGH",
            "MAIN, 2159, ULTRA_HIGH",
            "MAIN, 2160, FINAL",
            "DEV, 0, VERY_LOW",
            "DEV, 4, VERY_LOW",
            "DEV, 5, LOW",
            "DEV, 9, LOW",
            "DEV, 10, MEDIUM",
            "DEV, 49, MEDIUM",
            "DEV, 50, HIGH",
            "DEV, 79, HIGH",
            "DEV, 80, VERY_HIGH",
            "DEV, 99, VERY_HIGH",
            "DEV, 100, ULTRA_HIGH",
            "DEV, 119, ULTRA_HIGH",
            "DEV, 120, FINAL"
    })
    void getFinalityScore_shouldReturnCorrectScoreBasedOnNetworkAndSlots(String network, long slots, CardanoFinalityScore expectedScore) {
        // Arrange
        CardanoNetwork cardanoNetwork = CardanoNetwork.valueOf(network);
        cardanoFinalityProvider = new CardanoFinalityProvider(cardanoNetwork);

        // Act
        val result = cardanoFinalityProvider.getFinalityScore(slots);

        // Assert
        assertThat(result).isEqualTo(expectedScore);
    }

    @Test
    void getFinalityScore_shouldHandleEdgeCasesCorrectly() {
        // Arrange for MAINNET
        cardanoFinalityProvider = new CardanoFinalityProvider(CardanoNetwork.MAIN);

        // Act & Assert
        assertThat(cardanoFinalityProvider.getFinalityScore(0)).isEqualTo(CardanoFinalityScore.VERY_LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(4)).isEqualTo(CardanoFinalityScore.VERY_LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(2160)).isEqualTo(CardanoFinalityScore.FINAL);

        // Arrange for DEVNET
        cardanoFinalityProvider = new CardanoFinalityProvider(CardanoNetwork.DEV);

        // Act & Assert
        assertThat(cardanoFinalityProvider.getFinalityScore(0)).isEqualTo(CardanoFinalityScore.VERY_LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(4)).isEqualTo(CardanoFinalityScore.VERY_LOW);
        assertThat(cardanoFinalityProvider.getFinalityScore(120)).isEqualTo(CardanoFinalityScore.FINAL);
    }

    @Test
    void getFinalityScore_shouldReturnFinalForExtremeLargeSlotValues() {
        // Arrange for MAINNET
        cardanoFinalityProvider = new CardanoFinalityProvider(CardanoNetwork.MAIN);

        // Act & Assert
        assertThat(cardanoFinalityProvider.getFinalityScore(Long.MAX_VALUE)).isEqualTo(CardanoFinalityScore.FINAL);

        // Arrange for DEVNET
        cardanoFinalityProvider = new CardanoFinalityProvider(CardanoNetwork.DEV);

        // Act & Assert
        assertThat(cardanoFinalityProvider.getFinalityScore(Long.MAX_VALUE)).isEqualTo(CardanoFinalityScore.FINAL);
    }

}
