package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.util.Optional;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@Embeddable
public class IncomeStatementData implements Validable {

    private Revenues revenues;
    private COGS cogs;
    private OperatingExpenses operatingExpenses;
    private OperatingProfit operatingProfit;
    private TaxExpenses taxExpenses;

    @Override
    public boolean isValid() {
        return true;
    }

    public Optional<Revenues> getRevenues() {
        return Optional.ofNullable(revenues);
    }

    public Optional<COGS> getCogs() {
        return Optional.ofNullable(cogs);
    }

    public Optional<OperatingExpenses> getOperatingExpenses() {
        return Optional.ofNullable(operatingExpenses);
    }

    public Optional<OperatingProfit> getOperatingProfit() {
        return Optional.ofNullable(operatingProfit);
    }

    public Optional<TaxExpenses> getTaxExpenses() {
        return Optional.ofNullable(taxExpenses);
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class Revenues {

        private BigDecimal otherIncome;
        private BigDecimal buildOfLongTermProvision;

        public Optional<BigDecimal> getOtherIncome() {
            return Optional.ofNullable(otherIncome);
        }

        public Optional<BigDecimal> getBuildOfLongTermProvision() {
            return Optional.ofNullable(buildOfLongTermProvision);
        }
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class COGS {

        private BigDecimal costOfProvidingServices;

        public Optional<BigDecimal> getCostOfProvidingServices() {
            return Optional.ofNullable(costOfProvidingServices);
        }
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class OperatingExpenses {

        private BigDecimal personnelExpenses;
        private BigDecimal generalAndAdministrativeExpenses;
        private BigDecimal depreciationAndImpairmentLossesOnTangibleAssets;
        private BigDecimal amortizationOnIntangibleAssets;

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
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class OperatingProfit {

        private BigDecimal financeIncome;
        private BigDecimal financeExpenses;
        private BigDecimal realisedGainsOnSaleOfCryptocurrencies;
        private BigDecimal stakingRewardsIncome;
        private BigDecimal netIncomeOptionsSale;
        private BigDecimal extraordinaryExpenses;

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

        public Optional<BigDecimal> getExtraordinaryExpenses() {
            return Optional.ofNullable(extraordinaryExpenses);
        }
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class TaxExpenses {

        private BigDecimal incomeTaxExpense;

        public Optional<BigDecimal> getIncomeTaxExpense() {
            return Optional.ofNullable(incomeTaxExpense);
        }
    }

}