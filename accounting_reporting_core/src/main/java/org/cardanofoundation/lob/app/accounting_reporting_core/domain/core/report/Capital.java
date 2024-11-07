package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import lombok.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Capital {

    @Nullable
    private BigDecimal capital;
    @Nullable
    private BigDecimal retainedEarnings;

    public Optional<BigDecimal> getCapital() {
        return Optional.ofNullable(capital);
    }

    public Optional<BigDecimal> getRetainedEarnings() {
        return Optional.ofNullable(retainedEarnings);
    }

}
