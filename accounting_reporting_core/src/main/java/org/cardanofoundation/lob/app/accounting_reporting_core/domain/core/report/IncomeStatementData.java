package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class IncomeStatementData {

    @Nullable
    private Revenues revenues;

    @Nullable
    private COGS cogs;

    @Nullable
    private OperatingExpenses operatingExpenses;

    @Nullable
    private FinancialIncome financialIncome;

    @Nullable
    private ExtraordinaryIncome extraordinaryIncome;

    @Nullable
    private TaxExpenses taxExpenses;

    public Optional<Revenues> getRevenues() {
        return Optional.ofNullable(revenues);
    }

    public Optional<COGS> getCogs() {
        return Optional.ofNullable(cogs);
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

}
