package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.reconcilation;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Audited
public class ReconcilationViolation {

    @NotBlank
    @Getter
    @Setter
    private String transactionId;

    @NotNull
    @Enumerated(STRING)
    @Getter
    @Setter
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReconcilationRejectionCode rejectionCode;

    @Type(value = io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Nullable
    private String sourceDiff;

    @NotBlank
    @Getter
    @Setter
    private String transactionInternalNumber;

    @NotBlank
    @Getter
    @Setter
    private LocalDate transactionEntryDate;

    @NotBlank
    @Getter
    @Setter
    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransactionType transactionType;

    @NotNull
    @Getter
    @Setter
    private BigDecimal amountLcySum;

    public Optional<String> getSourceDiff() {
        return Optional.ofNullable(sourceDiff);
    }

    public void setSourceDiff(Optional<String> diff) {
        this.sourceDiff = diff.orElse(null);
    }

}
