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
public class IncomeStatementData {

    @Nullable
    private Revenues revenues;

    @Nullable
    private CostOfServicesAndGoods costOfServicesAndGoods;

    @Nullable
    private OperatingExpenses operatingExpenses;

    @Nullable
    private FinancialIncome financialIncome;

    @Nullable
    private ExtraordinaryIncome extraordinaryIncome;

    @Nullable
    private TaxExpenses taxExpenses;

    @Nullable
    private BigDecimal profitForTheYear;

    public Optional<Revenues> getRevenues() {
        return Optional.ofNullable(revenues);
    }

    public Optional<CostOfServicesAndGoods> getCostOfServicesAndGoods() {
        return Optional.ofNullable(costOfServicesAndGoods);
    }

    public Optional<OperatingExpenses> getOperatingExpenses() {
        return Optional.ofNullable(operatingExpenses);
    }

    public Optional<FinancialIncome> getFinancialIncome() {
        return Optional.ofNullable(financialIncome);
    }

    public Optional<TaxExpenses> getTaxExpenses() {
        return Optional.ofNullable(taxExpenses);
    }

    public Optional<BigDecimal> getProfitForTheYear() {
        return Optional.ofNullable(profitForTheYear);
    }

    public Optional<ExtraordinaryIncome> getExtraordinaryIncome() {
        return Optional.ofNullable(extraordinaryIncome);
    }

}
