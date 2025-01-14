package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

import lombok.*;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Capital {

    @Nullable
    private BigDecimal capital;

    @Nullable
    private BigDecimal profitForTheYear;

    @Nullable
    private BigDecimal resultsCarriedForward;

    public Optional<BigDecimal> getCapital() {
        return Optional.ofNullable(capital);
    }

    public Optional<BigDecimal> getResultsCarriedForward() {
        return Optional.ofNullable(resultsCarriedForward);
    }

    public Optional<BigDecimal> getProfitForTheYear() {
        return Optional.ofNullable(profitForTheYear);
    }

}
