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
public class FinancialIncome {

    @Nullable
    private BigDecimal financialRevenues;
    @Nullable
    private BigDecimal financialExpenses;
    @Nullable
    private BigDecimal realisedGainsOnSaleOfCryptocurrencies;
    @Nullable
    private BigDecimal stakingRewardsIncome;
    @Nullable
    private BigDecimal netIncomeOptionsSale;

    public Optional<BigDecimal> getFinancialRevenues() {
        return Optional.ofNullable(financialRevenues);
    }

    public Optional<BigDecimal> getFinancialExpenses() {
        return Optional.ofNullable(financialExpenses);
    }

    public Optional<BigDecimal> getRealisedGainsOnSaleOfCryptocurrencies() {
        return Optional.ofNullable(realisedGainsOnSaleOfCryptocurrencies);
    }

    public Optional<BigDecimal> getStakingRewardsIncome() {
        return Optional.ofNullable(stakingRewardsIncome);
    }

    public Optional<BigDecimal> getNetIncomeOptionsSale() {
        return Optional.ofNullable(netIncomeOptionsSale);
    }

}
