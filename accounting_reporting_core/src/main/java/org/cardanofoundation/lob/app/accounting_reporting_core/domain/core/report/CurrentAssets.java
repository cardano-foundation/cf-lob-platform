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
public class CurrentAssets {

    @Nullable
    private BigDecimal prepaymentsAndOtherShortTermAssets;
    @Nullable
    private BigDecimal otherReceivables;
    @Nullable
    private BigDecimal cryptoAssets;
    @Nullable
    private BigDecimal cashAndCashEquivalents;

    public Optional<BigDecimal> getPrepaymentsAndOtherShortTermAssets() {
        return Optional.ofNullable(prepaymentsAndOtherShortTermAssets);
    }

    public Optional<BigDecimal> getOtherReceivables() {
        return Optional.ofNullable(otherReceivables);
    }

    public Optional<BigDecimal> getCryptoAssets() {
        return Optional.ofNullable(cryptoAssets);
    }

    public Optional<BigDecimal> getCashAndCashEquivalents() {
        return Optional.ofNullable(cashAndCashEquivalents);
    }

}
