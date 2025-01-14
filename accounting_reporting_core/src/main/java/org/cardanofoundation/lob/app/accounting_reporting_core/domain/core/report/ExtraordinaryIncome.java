package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;



import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class ExtraordinaryIncome {

    @Nullable
    private BigDecimal extraordinaryExpenses;

    public Optional<BigDecimal> getExtraordinaryExpenses() {
        return Optional.ofNullable(extraordinaryExpenses);
    }

}
