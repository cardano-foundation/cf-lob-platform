package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report;

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
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Validable;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportMode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportRollupPeriodType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
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

@Entity(name = "accounting_reporting_core.report.ReportEntity")
@Table(name = "accounting_core_report")
@NoArgsConstructor
@AllArgsConstructor
@Audited
@EntityListeners({ AuditingEntityListener.class })
public class ReportEntity extends CommonEntity implements Persistable<String>, Validable {

    @Id
    @Column(name = "report_id", nullable = false, length = 64)
    @NotBlank
    @Getter
    @Setter
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
    @Getter
    @Setter
    private ReportType type;

    @Enumerated(STRING)
    @Column(name = "rollup_period", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NotNull
    @Getter
    @Setter
    private ReportRollupPeriodType rollupPeriod;

    @Column(name = "year", nullable = false)
    @Min(1900)
    @Max(4000)
    @Getter
    @Setter
    private Short year; // SMALLINT in PostgreSQL, mapped to Java's short

    @Column(name = "period")
    @Min(1)
    @Max(12)
    @Nullable
    private Short period; // SMALLINT in PostgreSQL, mapped to Java's short

    @Enumerated(STRING)
    @Column(name = "mode", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Getter
    @Setter
    private ReportMode mode; // USER or SYSTEM report

    @Column(name = "date", nullable = false)
    @NotNull
    @Getter
    @Setter
    private LocalDate date;

    @Embedded
    @AttributeOverrides({
            // Balance Sheet::Assets - Non-Current Assets
            @AttributeOverride(name = "assets.nonCurrentAssets.propertyPlantEquipment", column = @Column(name = "data_balance_sheet__assets_non_current_property_plant_equip")),
            @AttributeOverride(name = "assets.nonCurrentAssets.intangibleAssets", column = @Column(name = "data_income_statement__operating_expenses_depreciation_ta")),
            @AttributeOverride(name = "assets.nonCurrentAssets.investments", column = @Column(name = "data_balance_sheet__assets_non_current_investments")),
            @AttributeOverride(name = "assets.nonCurrentAssets.financialAssets", column = @Column(name = "data_balance_sheet__assets_non_current_financial_assets")),

            // Balance Sheet::Assets - Current Assets
            @AttributeOverride(name = "assets.currentAssets.prepaymentsAndOtherShortTermAssets", column = @Column(name = "data_balance_sheet__assets_current_prepayments_short_assets")),
            @AttributeOverride(name = "assets.currentAssets.otherReceivables", column = @Column(name = "data_balance_sheet__assets_current_other_receivables")),
            @AttributeOverride(name = "assets.currentAssets.cryptoAssets", column = @Column(name = "data_balance_sheet__assets_current_crypto_assets")),
            @AttributeOverride(name = "assets.currentAssets.cashAndCashEquivalents", column = @Column(name = "data_balance_sheet__assets_current_cash_and_equivalen")),

            // Balance Sheet::Liabilities - Non-Current Liabilities
            @AttributeOverride(name = "liabilities.nonCurrentLiabilities.provisions", column = @Column(name = "data_balance_sheet__liabilities_non_current_provisions")),

            // Balance Sheet::Liabilities - Current Liabilities
            @AttributeOverride(name = "liabilities.currentLiabilities.tradeAccountsPayables", column = @Column(name = "data_balance_sheet__liabilities_current_trade_accounts")),
            @AttributeOverride(name = "liabilities.currentLiabilities.otherCurrentLiabilities", column = @Column(name = "data_balance_sheet__liabilities_current_other_liabilit")),
            @AttributeOverride(name = "liabilities.currentLiabilities.accrualsAndShortTermProvisions", column = @Column(name = "data_balance_sheet__liabilities_current_accruals_and_short")),

            // Balance Sheet::Capital
            @AttributeOverride(name = "capital.capital", column = @Column(name = "data_balance_sheet__capital_capital")),
            @AttributeOverride(name = "capital.retainedEarnings", column = @Column(name = "data_balance_sheet__capital_retained_earnings"))
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
            @AttributeOverride(name = "operatingExpenses.generalAndAdministrativeExpenses", column = @Column(name = "data_income_statement__operating_expenses_general_admin_ex")),
            @AttributeOverride(name = "operatingExpenses.depreciationAndImpairmentLossesOnTangibleAssets", column = @Column(name = "data_income_statement__operating_expenses_depreciation_tang")),
            @AttributeOverride(name = "operatingExpenses.amortizationOnIntangibleAssets", column = @Column(name = "data_income_statement__operating_expenses_amortization_int")),

            // Financial Income
            @AttributeOverride(name = "financialIncome.financeIncome", column = @Column(name = "data_income_statement__financial_income_finance_income")),
            @AttributeOverride(name = "financialIncome.financeExpenses", column = @Column(name = "data_income_statement__financial_income_finance_expenses")),
            @AttributeOverride(name = "financialIncome.realisedGainsOnSaleOfCryptocurrencies", column = @Column(name = "data_income_statement__financial_income_realised_gains")),
            @AttributeOverride(name = "financialIncome.stakingRewardsIncome", column = @Column(name = "data_income_statement__financial_income_staking_rewards")),
            @AttributeOverride(name = "financialIncome.netIncomeOptionsSale", column = @Column(name = "data_income_statement__financial_income_net_income_opt")),

            // Extraordinary Income
            @AttributeOverride(name = "extraordinaryIncome.extraordinaryExpenses", column = @Column(name = "data_income_statement__operating_expenses_extraordin_exp")),

            // Tax Expenses
            @AttributeOverride(name = "taxExpenses.incomeTaxExpense", column = @Column(name = "data_income_statement__tax_expenses_income_tax_expense"))
    })
    @Nullable
    private IncomeStatementData incomeStatementReportData;

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

    public Optional<@Min(1) @Max(12) Short> getPeriod() {
        return Optional.ofNullable(period);
    }

    public void setPeriod(Optional<@Min(1) @Max(12) Short> period) {
        this.period = period.orElse(null);
    }

}
