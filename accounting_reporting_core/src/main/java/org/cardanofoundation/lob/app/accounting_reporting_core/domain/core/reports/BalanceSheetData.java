package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.cardanofoundation.lob.app.support.calc.BigDecimals;
import org.cardanofoundation.lob.app.support.calc.Summable;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@Embeddable
public class BalanceSheetData implements Validable {

    @Nullable
    private Assets assets;
    @Nullable
    private Liabilities liabilities;
    @Nullable
    private Capital capital;

    @Override
    public boolean isValid() {
        return getAssets().isPresent() && getLiabilities().isPresent() && getCapital().isPresent()
                && assets.getValue().equals(BigDecimals.sum(liabilities, capital));
    }

    public Optional<Assets> getAssets() {
        return Optional.ofNullable(assets);
    }

    public Optional<Liabilities> getLiabilities() {
        return Optional.ofNullable(liabilities);
    }

    public Optional<Capital> getCapital() {
        return Optional.ofNullable(capital);
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class Assets implements Summable {

        @Nullable
        private NonCurrentAssets nonCurrentAssets;
        @Nullable
        private CurrentAssets currentAssets;

        public BigDecimal getValue() {
            return BigDecimals.sum(nonCurrentAssets, currentAssets);
        }

        public Optional<NonCurrentAssets> getNonCurrentAssets() {
            return Optional.ofNullable(nonCurrentAssets);
        }

        public Optional<CurrentAssets> getCurrentAssets() {
            return Optional.ofNullable(currentAssets);
        }

        @AllArgsConstructor
        @Builder(toBuilder = true)
        @EqualsAndHashCode
        @ToString
        @NoArgsConstructor
        @Embeddable
        public static class NonCurrentAssets implements Summable {

            @Nullable
            private BigDecimal propertyPlantEquipment;
            @Nullable
            private BigDecimal intangibleAssets;
            @Nullable
            private BigDecimal investments;
            @Nullable
            private BigDecimal financialAssets;

            public BigDecimal getValue() {
                return BigDecimals.sum(propertyPlantEquipment, intangibleAssets, investments, financialAssets);
            }

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

        @AllArgsConstructor
        @Builder(toBuilder = true)
        @EqualsAndHashCode
        @ToString
        @NoArgsConstructor
        @Embeddable
        public static class CurrentAssets implements Summable {

            @Nullable
            private BigDecimal prepaymentsAndOtherShortTermAssets;
            @Nullable
            private BigDecimal otherReceivables;
            @Nullable
            private BigDecimal cryptoAssets;
            @Nullable
            private BigDecimal cashAndCashEquivalents;

            @Override
            public BigDecimal getValue() {
                return BigDecimals.sum(prepaymentsAndOtherShortTermAssets, otherReceivables, cryptoAssets, cashAndCashEquivalents);
            }

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
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class Liabilities implements Summable {

        @Nullable
        private NonCurrentLiabilities nonCurrentLiabilities;
        @Nullable
        private CurrentLiabilities currentLiabilities;

        @Override
        public BigDecimal getValue() {
            return BigDecimals.sum(nonCurrentLiabilities, currentLiabilities);
        }

        public Optional<NonCurrentLiabilities> getNonCurrentLiabilities() {
            return Optional.ofNullable(nonCurrentLiabilities);
        }

        public Optional<CurrentLiabilities> getCurrentLiabilities() {
            return Optional.ofNullable(currentLiabilities);
        }

        @AllArgsConstructor
        @Builder(toBuilder = true)
        @EqualsAndHashCode
        @ToString
        @NoArgsConstructor
        @Embeddable
        public static class NonCurrentLiabilities implements Summable {

            @Nullable
            private BigDecimal provisions;

            @Override
            public BigDecimal getValue() {
                return BigDecimals.sum(provisions);
            }

            public Optional<BigDecimal> getProvisions() {
                return Optional.ofNullable(provisions);
            }
        }

        @AllArgsConstructor
        @Builder(toBuilder = true)
        @EqualsAndHashCode
        @ToString
        @NoArgsConstructor
        @Embeddable
        public static class CurrentLiabilities implements Summable {

            @Nullable
            private BigDecimal tradeAccountsPayables;
            @Nullable
            private BigDecimal otherCurrentLiabilities;
            @Nullable
            private BigDecimal accrualsAndShortTermProvisions;

            @Override
            public BigDecimal getValue() {
                return BigDecimals.sum(tradeAccountsPayables, otherCurrentLiabilities, accrualsAndShortTermProvisions);
            }

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
    }

    @AllArgsConstructor
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @Embeddable
    public static class Capital implements Summable {

        @Nullable
        private BigDecimal capital;
        @Nullable
        private BigDecimal retainedEarnings;
        @Nullable
        private BigDecimal freeFoundationCapital;

        @Override
        public BigDecimal getValue() {
            return BigDecimals.sum(capital, retainedEarnings, freeFoundationCapital);
        }

        public Optional<BigDecimal> getCapital() {
            return Optional.ofNullable(capital);
        }

        public Optional<BigDecimal> getFreeFoundationCapital() {
            return Optional.ofNullable(freeFoundationCapital);
        }

        public Optional<BigDecimal> getRetainedEarnings() {
            return Optional.ofNullable(retainedEarnings);
        }
    }

}