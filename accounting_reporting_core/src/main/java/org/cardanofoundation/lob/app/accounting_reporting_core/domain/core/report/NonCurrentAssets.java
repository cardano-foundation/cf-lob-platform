package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import java.math.BigDecimal;
import java.util.Optional;

import jakarta.persistence.Embeddable;

import javax.annotation.Nullable;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@Embeddable
public class NonCurrentAssets  {

    @Nullable
    private BigDecimal propertyPlantEquipment;

    @Nullable
    private BigDecimal intangibleAssets;

    @Nullable
    private BigDecimal investments;

    @Nullable
    private BigDecimal financialAssets;

    public Optional<BigDecimal> getPropertyPlantEquipment() {
        return Optional.ofNullable(propertyPlantEquipment);
    }

    public Optional<BigDecimal> getIntangibleAssets() {
        return Optional.ofNullable(intangibleAssets);
    }

    public Optional<BigDecimal> getInvestments() {
        return Optional.ofNullable(investments);
    }

    public Optional<BigDecimal> getFinancialAssets() {
        return Optional.ofNullable(financialAssets);
    }

}
