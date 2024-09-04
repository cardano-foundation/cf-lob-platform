package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.annotation.Nullable;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ReconcilationViolation {

    @NotBlank
    @Getter
    @Setter
    private String transactionId;

    @NotNull
    @Enumerated(STRING)
    @Getter
    @Setter
    private ReconcilationRejectionCode rejectionCode;

    @Type(value = io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Nullable
    private String sourceDiff;

    @Type(value = io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Nullable
    private String sinkDiff;

    @NotBlank
    @Getter
    @Setter
    private String transactionInternalNumber;

    public Optional<String> getSourceDiff() {
        return Optional.ofNullable(sourceDiff);
    }

    public void setSourceDiff(Optional<String> diff) {
        this.sourceDiff = diff.orElse(null);
    }

    public Optional<String> getSinkDiff() {
        return Optional.ofNullable(sinkDiff);
    }

    public void setSinkDiff(Optional<String> sinkDiff) {
        this.sinkDiff = sinkDiff.orElse(null);
    }

}
