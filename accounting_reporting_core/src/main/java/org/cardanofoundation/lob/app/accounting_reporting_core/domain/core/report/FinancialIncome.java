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
public class FinancialIncome {

    @Nullable
    private BigDecimal financeIncome;
    @Nullable
    private BigDecimal financeExpenses;
    @Nullable
    private BigDecimal realisedGainsOnSaleOfCryptocurrencies;
    @Nullable
    private BigDecimal stakingRewardsIncome;
    @Nullable
    private BigDecimal netIncomeOptionsSale;

    public Optional<BigDecimal> getFinanceIncome() {
        return Optional.ofNullable(financeIncome);
    }

    public Optional<BigDecimal> getFinanceExpenses() {
        return Optional.ofNullable(financeExpenses);
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
