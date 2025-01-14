package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;



import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class CurrentLiabilities  {

    @Nullable
    private BigDecimal tradeAccountsPayables;
    @Nullable
    private BigDecimal otherCurrentLiabilities;
    @Nullable
    private BigDecimal accrualsAndShortTermProvisions;

    public Optional<BigDecimal> getTradeAccountsPayables() {
        return Optional.ofNullable(tradeAccountsPayables);
    }

    public Optional<BigDecimal> getOtherCurrentLiabilities() {
        return Optional.ofNullable(otherCurrentLiabilities);
    }

    public Optional<BigDecimal> getAccrualsAndShortTermProvisions() {
        return Optional.ofNullable(accrualsAndShortTermProvisions);
    }

}
