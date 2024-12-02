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
public class OperatingExpenses {

    @Nullable
    private BigDecimal personnelExpenses;

    @Nullable
    private BigDecimal generalAndAdministrativeExpenses;

    @Nullable
    private BigDecimal depreciationAndImpairmentLossesOnTangibleAssets;

    @Nullable
    private BigDecimal amortizationOnIntangibleAssets;

    @Nullable
    private BigDecimal rentExpenses;

    public Optional<BigDecimal> getPersonnelExpenses() {
        return Optional.ofNullable(personnelExpenses);
    }

    public Optional<BigDecimal> getGeneralAndAdministrativeExpenses() {
        return Optional.ofNullable(generalAndAdministrativeExpenses);
    }

    public Optional<BigDecimal> getDepreciationAndImpairmentLossesOnTangibleAssets() {
        return Optional.ofNullable(depreciationAndImpairmentLossesOnTangibleAssets);
    }

    public Optional<BigDecimal> getAmortizationOnIntangibleAssets() {
        return Optional.ofNullable(amortizationOnIntangibleAssets);
    }

    public Optional<BigDecimal> getRentExpenses() {
        return Optional.ofNullable(rentExpenses);
    }

}
