package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportMode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportRollupPeriodType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports.BalanceSheetData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports.IncomeStatementData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports.Validable;
import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.envers.Audited;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@jakarta.persistence.Entity(name = "accounting_reporting_core.ReportEntity")
@Table(name = "accounting_core_report")
@NoArgsConstructor
@AllArgsConstructor
@Audited
@EntityListeners({ AuditingEntityListener.class })
@Getter
@Setter
public class ReportEntity extends CommonEntity implements Persistable<String>, Validable {

    @Id
    @Column(name = "report_id", nullable = false, length = 64)
    @NotBlank
    private String reportId;

    @Override
    public String getId() {
        return reportId;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "name", column = @Column(name = "organisation_name")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "organisation_country_code")),
            @AttributeOverride(name = "taxIdNumber", column = @Column(name = "organisation_tax_id_number")),
            @AttributeOverride(name = "currencyId", column = @Column(name = "organisation_currency_id"))
    })
    @Getter
    @Setter
    private Organisation organisation;

    @Enumerated(STRING)
    @Column(name = "type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NotNull
    private ReportType type;

    @Enumerated(STRING)
    @Column(name = "rollup_period", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NotNull
    private ReportRollupPeriodType rollupPeriod;

    @Column(name = "year", nullable = false)
    @Min(1900)
    @Max(4000)
    private short year; // SMALLINT in PostgreSQL, mapped to Java's short

    @Column(name = "period", nullable = false)
    @Min(1)
    @Max(12)
    private short period; // SMALLINT, representing the month (1 - 12)

    @Enumerated(STRING)
    @Column(name = "mode", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReportMode mode; // USER or SYSTEM report

    @Column(name = "date", nullable = false)
    @NotNull
    private LocalDate date;

    @Embedded
    @AttributeOverrides({
            // Balance Sheet::Assets - Non-Current Assets
            @AttributeOverride(name = "assets.nonCurrentAssets.propertyPlantEquipment", column = @Column(name = "data_balance_sheet__assets_non_current_property_plant_equipment")),
            @AttributeOverride(name = "assets.nonCurrentAssets.intangibleAssets", column = @Column(name = "data_balance_sheet__assets_non_current_intangible_assets")),
            @AttributeOverride(name = "assets.nonCurrentAssets.investments", column = @Column(name = "data_balance_sheet__assets_non_current_investments")),
            @AttributeOverride(name = "assets.nonCurrentAssets.financialAssets", column = @Column(name = "data_balance_sheet__assets_non_current_financial_assets")),

            // Balance Sheet::Assets - Current Assets
            @AttributeOverride(name = "assets.currentAssets.prepaymentsAndOtherShortTermAssets", column = @Column(name = "data_balance_sheet__assets_current_prepayments_and_other_short_term_assets")),
            @AttributeOverride(name = "assets.currentAssets.otherReceivables", column = @Column(name = "data_balance_sheet__assets_current_other_receivables")),
            @AttributeOverride(name = "assets.currentAssets.cryptoAssets", column = @Column(name = "data_balance_sheet__assets_current_crypto_assets")),
            @AttributeOverride(name = "assets.currentAssets.cashAndCashEquivalents", column = @Column(name = "data_balance_sheet__assets_current_cash_and_cash_equivalents")),

            // Balance Sheet::Liabilities - Non-Current Liabilities
            @AttributeOverride(name = "liabilities.nonCurrentLiabilities.provisions", column = @Column(name = "data_balance_sheet__liabilities_non_current_provisions")),

            // Balance Sheet::Liabilities - Current Liabilities
            @AttributeOverride(name = "liabilities.currentLiabilities.tradeAccountsPayables", column = @Column(name = "data_balance_sheet__liabilities_current_trade_accounts_payables")),
            @AttributeOverride(name = "liabilities.currentLiabilities.otherCurrentLiabilities", column = @Column(name = "data_balance_sheet__liabilities_current_other_current_liabilities")),
            @AttributeOverride(name = "liabilities.currentLiabilities.accrualsAndShortTermProvisions", column = @Column(name = "data_balance_sheet__liabilities_current_accruals_and_short_term_provisions")),

            // Balance Sheet::Capital
            @AttributeOverride(name = "capital.capital", column = @Column(name = "data_balance_sheet__capital_capital")),
            @AttributeOverride(name = "capital.retainedEarnings", column = @Column(name = "data_balance_sheet__capital_retained_earnings")),
            @AttributeOverride(name = "capital.freeFoundationCapital", column = @Column(name = "data_balance_sheet__capital_free_foundation_capital"))
    })
    @Nullable
    private BalanceSheetData balanceSheetReportData;

    @Embedded
    @AttributeOverrides({
            // Revenues
            @AttributeOverride(name = "revenues.otherIncome", column = @Column(name = "data_income_statement__revenues_other_income")),
            @AttributeOverride(name = "revenues.buildOfLongTermProvision", column = @Column(name = "data_income_statement__revenues_build_long_term_provision")),

            // COGS (Cost of Goods Sold)
            @AttributeOverride(name = "cogs.costOfProvidingServices", column = @Column(name = "data_income_statement__cogs_cost_providing_services")),

            // Operating Expenses
            @AttributeOverride(name = "operatingExpenses.personnelExpenses", column = @Column(name = "data_income_statement__operating_expenses_personnel_expenses")),
            @AttributeOverride(name = "operatingExpenses.generalAndAdministrativeExpenses", column = @Column(name = "data_income_statement__operating_expenses_general_administrative_expenses")),
            @AttributeOverride(name = "operatingExpenses.depreciationAndImpairmentLossesOnTangibleAssets", column = @Column(name = "data_income_statement__operating_expenses_depreciation_impairment_tangible_assets")),
            @AttributeOverride(name = "operatingExpenses.amortizationOnIntangibleAssets", column = @Column(name = "data_income_statement__operating_expenses_amortization_intangible_assets")),

            // Operating Profit
            @AttributeOverride(name = "operatingProfit.financeIncome", column = @Column(name = "data_income_statement__operating_profit_finance_income")),
            @AttributeOverride(name = "operatingProfit.financeExpenses", column = @Column(name = "data_income_statement__operating_profit_finance_expenses")),
            @AttributeOverride(name = "operatingProfit.realisedGainsOnSaleOfCryptocurrencies", column = @Column(name = "data_income_statement__operating_profit_realised_gains_sale_cryptocurrencies")),
            @AttributeOverride(name = "operatingProfit.stakingRewardsIncome", column = @Column(name = "data_income_statement__operating_profit_staking_rewards_income")),
            @AttributeOverride(name = "operatingProfit.netIncomeOptionsSale", column = @Column(name = "data_income_statement__operating_profit_net_income_options_sale")),
            @AttributeOverride(name = "operatingProfit.extraordinaryExpenses", column = @Column(name = "data_income_statement__operating_profit_extraordinary_expenses")),

            // Tax Expenses
            @AttributeOverride(name = "taxExpenses.incomeTaxExpense", column = @Column(name = "data_income_statement__tax_expenses_income_tax_expense"))
    })
    @Nullable
    private IncomeStatementData incomeStatementReportData;

    @Column(name = "report_approved", nullable = false)
    @Getter
    @Setter
    private Boolean reportApproved = false;

    @Column(name = "ledger_dispatch_approved", nullable = false)
    @Getter
    @Setter
    private Boolean ledgerDispatchApproved = false;

    @Column(name = "ledger_dispatch_status", nullable = false)
    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Getter
    @Setter
    // https://www.baeldung.com/java-enums-jpa-postgresql
    private LedgerDispatchStatus ledgerDispatchStatus = NOT_DISPATCHED;

    public static String id(String organisationId,
                            ReportType reportType,
                            ReportRollupPeriodType rollupPeriod,
                            short year,
                            Optional<Short> period) {
        return period.map(p -> {
            return digestAsHex(STR."\{organisationId}::\{reportType}::\{rollupPeriod}::\{year}::\{p}");
        }).orElseGet(() -> {
            return digestAsHex(STR."\{organisationId}::\{reportType}::\{rollupPeriod}::\{year}");
        });
    }

    public void setBalanceSheetReportData(Optional<BalanceSheetData> balanceSheetReportData) {
        if (type != ReportType.BALANCE_SHEET) {
            throw new IllegalStateException("Report type is not BALANCE_SHEET!");
        }

        this.balanceSheetReportData = balanceSheetReportData.orElse(null);
    }

    public void setIncomeStatementReportData(Optional<IncomeStatementData> incomeStatementReportData) {
        if (type != ReportType.INCOME_STATEMENT) {
            throw new IllegalStateException("Report type is not INCOME_STATEMENT!");
        }

        this.incomeStatementReportData = incomeStatementReportData.orElse(null);
    }

    public Optional<BalanceSheetData> getBalanceSheetReportData() {
        return Optional.ofNullable(balanceSheetReportData);
    }

    public Optional<IncomeStatementData> getIncomeStatementReportData() {
        return Optional.ofNullable(incomeStatementReportData);
    }

    public Optional<Validable> getReportData() {
        return switch (type) {
            case BALANCE_SHEET -> getBalanceSheetReportData().map(Validable.class::cast);
            case INCOME_STATEMENT -> getIncomeStatementReportData().map(Validable.class::cast);
        };
    }

    @Override
    public boolean isValid() {
        return switch (type) {
            case BALANCE_SHEET -> getBalanceSheetReportData().map(Validable::isValid).orElse(false);
            case INCOME_STATEMENT -> getIncomeStatementReportData().map(Validable::isValid).orElse(false);
        };
    }

}
