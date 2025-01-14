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
public class Revenues {

    @Nullable
    private BigDecimal otherIncome;
    @Nullable
    private BigDecimal buildOfLongTermProvision;

    public Optional<BigDecimal> getOtherIncome() {
        return Optional.ofNullable(otherIncome);
    }

    public Optional<BigDecimal> getBuildOfLongTermProvision() {
        return Optional.ofNullable(buildOfLongTermProvision);
    }

}
