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
public class NonCurrentLiabilities {

    @Nullable
    private BigDecimal provisions;

    public Optional<BigDecimal> getProvisions() {
        return Optional.ofNullable(provisions);
    }

}
