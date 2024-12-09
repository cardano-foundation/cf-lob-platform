package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.IntervalType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BalanceSheetRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.IncomeStatementRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReportRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReportSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReportResponseView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReportingParametersView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.ReportService;
import org.cardanofoundation.lob.app.organisation.service.OrganisationCurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    private final OrganisationCurrencyService organisationCurrencyService;
    private final ReportService reportService;


    @Tag(name = "Reporting", description = "Report Parameters")
    @GetMapping(value = "/report-parameters/{orgId}", produces = "application/json")
    public ResponseEntity<?> reportParameters(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {

        val cur = organisationCurrencyService.findAllByOrganisationId(orgId)
                .stream()
                .map(organisationCurrency -> {
                    return organisationCurrency.getId() != null ? organisationCurrency.getId().getCustomerCode() : null;
                }).collect(Collectors.toSet());
        return ResponseEntity.ok().body(
                new ReportingParametersView(
                        Arrays.stream(ReportType.values()).collect(Collectors.toSet()),
                        Arrays.stream(IntervalType.values()).collect(Collectors.toSet()),
                        cur
                )
        );
    }

    @Tag(name = "Reporting", description = "Create Balance Sheet")
    @PostMapping(value = "/report-create", produces = "application/json")
    public ResponseEntity<?> reportCreate(@Valid @RequestBody ReportRequest reportSaveRequest) {

        if (reportSaveRequest.getReportType().equals(ReportType.INCOME_STATEMENT)) {
            return reportService.storeIncomeStatement(
                    reportSaveRequest.getOrganisationID(),
                    reportSaveRequest.getOtherIncome(),
                    reportSaveRequest.getBuildOfLongTermProvision(),
                    reportSaveRequest.getCostOfProvidingServices(),
                    reportSaveRequest.getFinancialRevenues(),
                    reportSaveRequest.getNetIncomeOptionsSale(),
                    reportSaveRequest.getRealisedGainsOnSaleOfCryptocurrencies(),
                    reportSaveRequest.getStakingRewardsIncome(),
                    reportSaveRequest.getFinancialExpenses(),
                    reportSaveRequest.getExtraordinaryExpenses(),
                    reportSaveRequest.getIncomeTaxExpense(),
                    reportSaveRequest.getPersonnelExpenses(),
                    reportSaveRequest.getGeneralAndAdministrativeExpenses(),
                    reportSaveRequest.getDepreciationAndImpairmentLossesOnTangibleAssets(),
                    reportSaveRequest.getAmortizationOnIntangibleAssets(),
                    reportSaveRequest.getRentExpenses(),
                    reportSaveRequest.getReportType(),
                    reportSaveRequest.getIntervalType(),
                    reportSaveRequest.getYear(),
                    reportSaveRequest.getPeriod()
            ).fold(problem -> {
                return ResponseEntity.status(problem.getStatus().getStatusCode()).body(problem);
            }, success -> {
                return ResponseEntity.ok().build();
            });
        }
        return reportService.storeBalanceSheet(
                reportSaveRequest.getOrganisationID(),
                reportSaveRequest.getPropertyPlantEquipment(),
                reportSaveRequest.getIntangibleAssets(),
                reportSaveRequest.getInvestments(),
                reportSaveRequest.getFinancialAssets(),
                reportSaveRequest.getPrepaymentsAndOtherShortTermAssets(),
                reportSaveRequest.getOtherReceivables(),
                reportSaveRequest.getCryptoAssets(),
                reportSaveRequest.getCashAndCashEquivalents(),
                reportSaveRequest.getProvisions(),
                reportSaveRequest.getTradeAccountsPayables(),
                reportSaveRequest.getOtherCurrentLiabilities(),
                reportSaveRequest.getAccrualsAndShortTermProvisions(),
                reportSaveRequest.getCapital(),
                reportSaveRequest.getProfitForTheYear(),
                reportSaveRequest.getResultsCarriedForward(),
                reportSaveRequest.getReportType(),
                reportSaveRequest.getIntervalType(),
                reportSaveRequest.getYear(),
                reportSaveRequest.getPeriod()
        ).fold(problem -> {
            return ResponseEntity.status(problem.getStatus().getStatusCode()).body(problem);
        }, success -> {
            return ResponseEntity.ok().build();
        });
    }


    @Tag(name = "Reporting", description = "Create Income Statement")
    @PostMapping(value = "/report-search", produces = "application/json")
    public ResponseEntity<?> reportSearch(@Valid @RequestBody ReportSearchRequest reportSearchRequest) {

        return reportService.exist(
                reportSearchRequest.getOrganisationID(),
                reportSearchRequest.getReportType(),
                reportSearchRequest.getIntervalType(),
                reportSearchRequest.getYear(),
                reportSearchRequest.getPeriod()
        ).fold(problem -> {
            return ResponseEntity.status(problem.getStatus().getStatusCode()).body(problem);
        }, success -> {
            val este = new ReportResponseView();
            este.setReportId(success.getReportId());
            este.setOrganisationId(success.getOrganisation().getId());
            este.setType(success.getType());
            este.setIntervalType(success.getIntervalType());
            este.setYear(success.getYear());
            este.setPeriod(success.getPeriod());
            este.setDate(success.getDate());
            este.setPropertyPlantEquipment(String.valueOf(success.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getNonCurrentAssets().flatMap(nonCurrentAssets -> nonCurrentAssets.getPropertyPlantEquipment())))));
            este.setIntangibleAssets(String.valueOf(success.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getNonCurrentAssets().flatMap(nonCurrentAssets -> nonCurrentAssets.getIntangibleAssets())))));
            return ResponseEntity.ok().body(
                    este
            );
        });
    }


}
