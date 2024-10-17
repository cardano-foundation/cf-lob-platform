package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Audited
public class FilteringParameters {

    @NotNull
    private String organisationId;

    @Builder.Default
    @NotNull
    private List<TransactionType> transactionTypes = List.of();

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

    @Nullable // nullable since when batch fails we don't always have this info (it is resolved from the org)
    private LocalDate accountingPeriodFrom;

    @Nullable // nullable since when batch fails we don't always have this info (it is resolved from the org)
    private LocalDate accountingPeriodTo;

    @Builder.Default
    @NotNull
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> transactionNumbers = List.of();

}
