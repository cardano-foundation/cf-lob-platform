package org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service;


import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.vavr.control.Either;
import org.zalando.problem.Problem;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReportPublishRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReportRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReportView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.ReportService;

@Service
@org.jmolecules.ddd.annotation.Service
@Slf4j
@RequiredArgsConstructor
@Transactional()
public class ReportViewService {
    private final ReportService reportService;

    @Transactional
    public Either<Problem, ReportEntity> reportPublish(ReportPublishRequest reportPublish) {
        return reportService.approveReportForLedgerDispatch(reportPublish.getReportId());
    }

    @Transactional
    public Either<Problem, ReportEntity> reportCreate(ReportRequest reportSaveRequest) {
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
            );
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
        );

    }

    public ReportView responseView(ReportEntity reportEntity) {
        val reportResponseView = new ReportView();
        reportResponseView.setReportId(reportEntity.getReportId());
        reportResponseView.setOrganisationId(reportEntity.getOrganisation().getId());
        reportResponseView.setType(reportEntity.getType());
        reportResponseView.setIntervalType(reportEntity.getIntervalType());
        reportResponseView.setYear(reportEntity.getYear());
        reportResponseView.setPeriod(reportEntity.getPeriod());
        reportResponseView.setDate(reportEntity.getDate());
        reportResponseView.setPublish(reportEntity.getLedgerDispatchApproved());
        reportResponseView.setCanBePublish(true);
        Either<Problem, Boolean> left = reportService.canPublish(reportEntity);
        reportResponseView.setError(Optional.empty());
        if (left.isLeft()) {
            reportResponseView.setCanBePublish(false);
            reportResponseView.setError(Optional.of(left.getLeft()));
        }
        reportResponseView.setVer(reportEntity.getVer());
        //BalanceSheet
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getNonCurrentAssets().flatMap(nonCurrentAssets -> nonCurrentAssets.getPropertyPlantEquipment()))).ifPresent(bigDecimal -> reportResponseView.setPropertyPlantEquipment(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getNonCurrentAssets().flatMap(nonCurrentAssets -> nonCurrentAssets.getIntangibleAssets()))).ifPresent(bigDecimal -> reportResponseView.setIntangibleAssets(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getNonCurrentAssets().flatMap(nonCurrentAssets -> nonCurrentAssets.getInvestments()))).ifPresent(bigDecimal -> reportResponseView.setInvestments(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getNonCurrentAssets().flatMap(nonCurrentAssets -> nonCurrentAssets.getFinancialAssets()))).ifPresent(bigDecimal -> reportResponseView.setFinancialAssets(bigDecimal.toString()));

        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getCurrentAssets().flatMap(currentAssets -> currentAssets.getPrepaymentsAndOtherShortTermAssets()))).ifPresent(bigDecimal -> reportResponseView.setPrepaymentsAndOtherShortTermAssets(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getCurrentAssets().flatMap(currentAssets -> currentAssets.getOtherReceivables()))).ifPresent(bigDecimal -> reportResponseView.setOtherReceivables(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getCurrentAssets().flatMap(currentAssets -> currentAssets.getCryptoAssets()))).ifPresent(bigDecimal -> reportResponseView.setCryptoAssets(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getAssets().flatMap(assets -> assets.getCurrentAssets().flatMap(currentAssets -> currentAssets.getCashAndCashEquivalents()))).ifPresent(bigDecimal -> reportResponseView.setCashAndCashEquivalents(bigDecimal.toString()));

        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getLiabilities().flatMap(liabilities -> liabilities.getNonCurrentLiabilities().flatMap(nonCurrentLiabilities -> nonCurrentLiabilities.getProvisions()))).ifPresent(bigDecimal -> reportResponseView.setProvisions(bigDecimal.toString()));

        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getLiabilities().flatMap(liabilities -> liabilities.getCurrentLiabilities().flatMap(currentLiabilities -> currentLiabilities.getTradeAccountsPayables()))).ifPresent(bigDecimal -> reportResponseView.setTradeAccountsPayables(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getLiabilities().flatMap(liabilities -> liabilities.getCurrentLiabilities().flatMap(currentLiabilities -> currentLiabilities.getOtherCurrentLiabilities()))).ifPresent(bigDecimal -> reportResponseView.setOtherCurrentLiabilities(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getLiabilities().flatMap(liabilities -> liabilities.getCurrentLiabilities().flatMap(currentLiabilities -> currentLiabilities.getAccrualsAndShortTermProvisions()))).ifPresent(bigDecimal -> reportResponseView.setAccrualsAndShortTermProvisions(bigDecimal.toString()));

        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getCapital().flatMap(capital -> capital.getCapital())).ifPresent(bigDecimal -> reportResponseView.setCapital(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getCapital().flatMap(capital -> capital.getProfitForTheYear())).ifPresent(bigDecimal -> reportResponseView.setProfitForTheYear(bigDecimal.toString()));
        reportEntity.getBalanceSheetReportData().flatMap(balanceSheetData -> balanceSheetData.getCapital().flatMap(capital -> capital.getResultsCarriedForward())).ifPresent(bigDecimal -> reportResponseView.setResultsCarriedForward(bigDecimal.toString()));

        //IncomeStatement
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getRevenues().flatMap(revenues -> revenues.getOtherIncome())).ifPresent(bigDecimal -> reportResponseView.setOtherIncome(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getRevenues().flatMap(revenues -> revenues.getBuildOfLongTermProvision())).ifPresent(bigDecimal -> reportResponseView.setBuildOfLongTermProvision(bigDecimal.toString()));

        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getCostOfGoodsAndServices().flatMap(costOfGoodsAndServices -> costOfGoodsAndServices.getCostOfProvidingServices())).ifPresent(bigDecimal -> reportResponseView.setCostOfProvidingServices(bigDecimal.toString()));

        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getOperatingExpenses().flatMap(operatingExpenses -> operatingExpenses.getPersonnelExpenses())).ifPresent(bigDecimal -> reportResponseView.setPersonnelExpenses(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getOperatingExpenses().flatMap(operatingExpenses -> operatingExpenses.getGeneralAndAdministrativeExpenses())).ifPresent(bigDecimal -> reportResponseView.setGeneralAndAdministrativeExpenses(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getOperatingExpenses().flatMap(operatingExpenses -> operatingExpenses.getDepreciationAndImpairmentLossesOnTangibleAssets())).ifPresent(bigDecimal -> reportResponseView.setDepreciationAndImpairmentLossesOnTangibleAssets(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getOperatingExpenses().flatMap(operatingExpenses -> operatingExpenses.getAmortizationOnIntangibleAssets())).ifPresent(bigDecimal -> reportResponseView.setAmortizationOnIntangibleAssets(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getOperatingExpenses().flatMap(operatingExpenses -> operatingExpenses.getRentExpenses())).ifPresent(bigDecimal -> reportResponseView.setRentExpenses(bigDecimal.toString()));

        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getFinancialIncome().flatMap(financialIncome -> financialIncome.getFinancialRevenues())).ifPresent(bigDecimal -> reportResponseView.setFinancialRevenues(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getFinancialIncome().flatMap(financialIncome -> financialIncome.getFinancialExpenses())).ifPresent(bigDecimal -> reportResponseView.setFinancialExpenses(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getFinancialIncome().flatMap(financialIncome -> financialIncome.getRealisedGainsOnSaleOfCryptocurrencies())).ifPresent(bigDecimal -> reportResponseView.setRealisedGainsOnSaleOfCryptocurrencies(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getFinancialIncome().flatMap(financialIncome -> financialIncome.getStakingRewardsIncome())).ifPresent(bigDecimal -> reportResponseView.setStakingRewardsIncome(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getFinancialIncome().flatMap(financialIncome -> financialIncome.getNetIncomeOptionsSale())).ifPresent(bigDecimal -> reportResponseView.setNetIncomeOptionsSale(bigDecimal.toString()));

        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getExtraordinaryIncome().flatMap(extraordinaryIncome -> extraordinaryIncome.getExtraordinaryExpenses())).ifPresent(bigDecimal -> reportResponseView.setExtraordinaryExpenses(bigDecimal.toString()));

        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getTaxExpenses().flatMap(taxExpenses -> taxExpenses.getIncomeTaxExpense())).ifPresent(bigDecimal -> reportResponseView.setIncomeTaxExpense(bigDecimal.toString()));
        reportEntity.getIncomeStatementReportData().flatMap(incomeStatementData -> incomeStatementData.getProfitForTheYear()).ifPresent(bigDecimal -> reportResponseView.setProfitForTheYear(bigDecimal.toString()));

        return reportResponseView;
    }
}
