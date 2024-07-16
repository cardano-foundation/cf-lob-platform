package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;

import javax.annotation.Nullable;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
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
    private OnChainAssuranceLevel assuranceLevel;

    public Optional<String> getTransactionHash() {
        return Optional.ofNullable(transactionHash);
    }

    public Optional<Long> getAbsoluteSlot() {
        return Optional.ofNullable(absoluteSlot);
    }

    public Optional<Long> getCreationSlot() {
        return Optional.ofNullable(creationSlot);
    }

    public Optional<OnChainAssuranceLevel> getAssuranceLevel() {
        return Optional.ofNullable(assuranceLevel);
    }

    public Optional<BlockchainPublishStatus> getPublishStatus() {
        return Optional.ofNullable(publishStatus);
    }

}
