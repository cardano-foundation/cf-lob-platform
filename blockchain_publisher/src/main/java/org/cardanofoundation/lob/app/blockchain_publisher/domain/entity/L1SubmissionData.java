package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;

import javax.annotation.Nullable;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
public class L1SubmissionData {

    @Nullable
    private String transactionHash;

    @Nullable
    private Long absoluteSlot;

    @Nullable
    private Long creationSlot;

    @Nullable
    @Enumerated(STRING)
    private BlockchainPublishStatus publishStatus;

    @Nullable
    @Enumerated(STRING)
    private CardanoFinalityScore finalityScore;

    public Optional<String> getTransactionHash() {
        return Optional.ofNullable(transactionHash);
    }

    public Optional<Long> getAbsoluteSlot() {
        return Optional.ofNullable(absoluteSlot);
    }

    public Optional<Long> getCreationSlot() {
        return Optional.ofNullable(creationSlot);
    }

    public Optional<CardanoFinalityScore> getFinalityScore() {
        return Optional.ofNullable(finalityScore);
    }

    public Optional<BlockchainPublishStatus> getPublishStatus() {
        return Optional.ofNullable(publishStatus);
    }

    public void setFinalityScore(Optional<CardanoFinalityScore> assuranceLevel) {
        this.finalityScore = assuranceLevel.orElse(null);
    }

    public void setPublishStatus(Optional<BlockchainPublishStatus> publishStatus) {
        this.publishStatus = publishStatus.orElse(null);
    }

    public boolean isFinalized() {
        return finalityScore != null && finalityScore.equals(CardanoFinalityScore.FINAL);
    }

}
