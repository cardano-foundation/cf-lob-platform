package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReportRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.CreateReportView;

@ExtendWith(MockitoExtension.class)
class CreateReportViewTest {

    private ReportRequest reportRequest;

    @BeforeEach
    void setUp() {
        reportRequest = mock(ReportRequest.class);
        when(reportRequest.getOrganisationID()).thenReturn("org-123");
    }

    @Test
    void fromReportRequest_WithBalanceSheet_ShouldPopulateBalanceSheetData() {
        when(reportRequest.getReportType()).thenReturn(ReportType.BALANCE_SHEET);
        when(reportRequest.getPropertyPlantEquipment()).thenReturn("1000");
        when(reportRequest.getIntangibleAssets()).thenReturn("500");
        when(reportRequest.getInvestments()).thenReturn("200");
        when(reportRequest.getFinancialAssets()).thenReturn("300");
        when(reportRequest.getPrepaymentsAndOtherShortTermAssets()).thenReturn("150");
        when(reportRequest.getOtherReceivables()).thenReturn("250");
        when(reportRequest.getCryptoAssets()).thenReturn("350");
        when(reportRequest.getCashAndCashEquivalents()).thenReturn("450");
        when(reportRequest.getProvisions()).thenReturn("50");
        when(reportRequest.getTradeAccountsPayables()).thenReturn("60");
        when(reportRequest.getOtherCurrentLiabilities()).thenReturn("70");
        when(reportRequest.getAccrualsAndShortTermProvisions()).thenReturn("80");
        when(reportRequest.getCapital()).thenReturn("1000");
        when(reportRequest.getProfitForTheYear()).thenReturn("500");
        when(reportRequest.getResultsCarriedForward()).thenReturn("200");

        CreateReportView view = CreateReportView.fromReportRequest(reportRequest);

        Assertions.assertEquals("org-123", view.getOrganisationId());
        Assertions.assertTrue(view.getBalanceSheetData().isPresent());
        assertEquals(new BigDecimal("1000"), view.getBalanceSheetData().get().getCapital().get().getCapital().get());
    }

    @Test
    void fromReportRequest_WithIncomeStatement_ShouldPopulateIncomeStatementData() {
        when(reportRequest.getReportType()).thenReturn(ReportType.INCOME_STATEMENT);
        when(reportRequest.getOtherIncome()).thenReturn("1000");
        when(reportRequest.getBuildOfLongTermProvision()).thenReturn("500");
        when(reportRequest.getCostOfProvidingServices()).thenReturn("200");
        when(reportRequest.getFinancialRevenues()).thenReturn("300");
        when(reportRequest.getNetIncomeOptionsSale()).thenReturn("150");
        when(reportRequest.getRealisedGainsOnSaleOfCryptocurrencies()).thenReturn("250");
        when(reportRequest.getStakingRewardsIncome()).thenReturn("350");
        when(reportRequest.getExtraordinaryExpenses()).thenReturn("50");
        when(reportRequest.getIncomeTaxExpense()).thenReturn("60");
        when(reportRequest.getPersonnelExpenses()).thenReturn("70");
        when(reportRequest.getGeneralAndAdministrativeExpenses()).thenReturn("80");
        when(reportRequest.getDepreciationAndImpairmentLossesOnTangibleAssets()).thenReturn("90");
        when(reportRequest.getAmortizationOnIntangibleAssets()).thenReturn("100");
        when(reportRequest.getRentExpenses()).thenReturn("110");

        CreateReportView view = CreateReportView.fromReportRequest(reportRequest);

        assertEquals("org-123", view.getOrganisationId());
        assertTrue(view.getIncomeStatementData().isPresent());
        assertEquals(new BigDecimal("1000"), view.getIncomeStatementData().get().getRevenues().get().getOtherIncome().get());
    }

    @Test
    void fromReportRequest_WithUnknownReportType_ShouldHaveNoReportData() {
        when(reportRequest.getReportType()).thenReturn(null);

        CreateReportView view = CreateReportView.fromReportRequest(reportRequest);

        assertEquals("org-123", view.getOrganisationId());
        assertTrue(view.getBalanceSheetData().isEmpty());
        assertTrue(view.getIncomeStatementData().isEmpty());
    }

}
