package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;



import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class TaxExpenses {

    @Nullable
    private BigDecimal incomeTaxExpense;

    public Optional<BigDecimal> getIncomeTaxExpense() {
        return Optional.ofNullable(incomeTaxExpense);
    }

}
