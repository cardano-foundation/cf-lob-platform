package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.IntervalType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;

@Getter
@Setter
@AllArgsConstructor
//@Builder todo: For testing
@NoArgsConstructor
@Slf4j
public class IncomeStatementRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    private String organisationID;

    @Schema(example = "INCOME_STATEMENT")
    private ReportType reportType;

    private IntervalType intervalType;

    @Schema(example = "2024")
    private short year;

    @Schema(example = "3")
    private short period;

    @Schema(example = "10000.90")
    private String otherIncome;

    @Schema(example = "1000000.10")
    private String buildOfLongTermProvision;

    @Schema(example = "500000.15")
    private String costOfProvidingServices;

    @Schema(example = "200000.53")
    private String financialRevenues;

    @Schema(example = "100000.10")
    private String netIncomeOptionsSale;

    @Schema(example = "50000.15")
    private String realisedGainsOnSaleOfCryptocurrencies;

    @Schema(example = "10000.53")
    private String stakingRewardsIncome;

    @Schema(example = "20000.10")
    private String financialExpenses;

    @Schema(example = "10000.10")
    private String extraordinaryExpenses;

    @Schema(example = "1000.51")
    private String incomeTaxExpense;

    @Schema(example = "500000.15")
    private String personnelExpenses;

    @Schema(example = "200000.53")
    private String generalAndAdministrativeExpenses;

    @Schema(example = "200000.53")
    private String depreciationAndImpairmentLossesOnTangibleAssets;

    @Schema(example = "200000.53")
    private String amortizationOnIntangibleAssets;

    @Schema(example = "200000.53")
    private String rentExpenses;

}
