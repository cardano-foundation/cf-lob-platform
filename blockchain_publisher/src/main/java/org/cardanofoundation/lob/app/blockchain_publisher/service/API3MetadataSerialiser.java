package org.cardanofoundation.lob.app.blockchain_publisher.service;


import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.stereotype.Service;

import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataMap;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.BalanceSheetData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.IncomeStatementData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;

@Service
@RequiredArgsConstructor
public class API3MetadataSerialiser {

    public static final String VERSION = "1.1";
    private final Clock clock;

    public MetadataMap serialiseToMetadataMap(ReportEntity reportEntity,
                                              long creationSlot) {
        val globalMetadataMap = MetadataBuilder.createMap();

        // Metadata Section
        globalMetadataMap.put("metadata", createMetadataSection(creationSlot));

        // Organisation Section
        val organisation = reportEntity.getOrganisation();
        globalMetadataMap.put("org", serialiseOrganisation(organisation));

        // Report Data Section
        globalMetadataMap.put("type", "REPORT");
        globalMetadataMap.put("subType", reportEntity.getType().name());
        globalMetadataMap.put("interval", reportEntity.getIntervalType().name());
        globalMetadataMap.put("year", reportEntity.getYear().toString());
        globalMetadataMap.put("mode", reportEntity.getMode().name());
        globalMetadataMap.put("ver", BigInteger.valueOf(reportEntity.getVer()));

        reportEntity.getPeriod().ifPresent(period -> globalMetadataMap.put("period", BigInteger.valueOf(period)));

        // Data Section
        switch (reportEntity.getType()) {
            case BALANCE_SHEET -> globalMetadataMap.put("data", serialiseBalanceSheetData(
                    reportEntity.getBalanceSheetReportData().orElseThrow()));
            case INCOME_STATEMENT -> globalMetadataMap.put("data", serialiseIncomeStatementData(
                    reportEntity.getIncomeStatementReportData().orElseThrow()));
            default -> throw new IllegalArgumentException(STR."Unsupported report type: \{reportEntity.getType()}");
        }

        return globalMetadataMap;
    }

    private MetadataMap createMetadataSection(long creationSlot) {
        val metadataMap = MetadataBuilder.createMap();
        val now = Instant.now(clock);

        metadataMap.put("creation_slot", BigInteger.valueOf(creationSlot));
        metadataMap.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(now));
        metadataMap.put("version", VERSION);

        return metadataMap;
    }

    private static MetadataMap serialiseOrganisation(org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.Organisation organisation) {
        val orgMap = MetadataBuilder.createMap();

        orgMap.put("id", organisation.getId());
        orgMap.put("name", organisation.getName());
        orgMap.put("tax_id_number", organisation.getTaxIdNumber());
        orgMap.put("currency_id", organisation.getCurrencyId());
        orgMap.put("country_code", organisation.getCountryCode());

        return orgMap;
    }

    private static MetadataMap serialiseBalanceSheetData(BalanceSheetData balanceSheetData) {
        val dataMap = MetadataBuilder.createMap();

        // Assets
        val assetsMap = MetadataBuilder.createMap();
        balanceSheetData.getAssets().ifPresent(assets -> {
            assets.getNonCurrentAssets().ifPresent(nca -> {
                val nonCurrentAssetsMap = MetadataBuilder.createMap();
                nca.getPropertyPlantEquipment().ifPresent(value -> nonCurrentAssetsMap.put("property_plant_equipment", value.toString()));
                nca.getIntangibleAssets().ifPresent(value -> nonCurrentAssetsMap.put("intangible_assets", value.toString()));
                nca.getInvestments().ifPresent(value -> nonCurrentAssetsMap.put("investments", value.toString()));
                nca.getFinancialAssets().ifPresent(value -> nonCurrentAssetsMap.put("financial_assets", value.toString()));
                assetsMap.put("non_current_assets", nonCurrentAssetsMap);
            });

            assets.getCurrentAssets().ifPresent(ca -> {
                val currentAssetsMap = MetadataBuilder.createMap();
                ca.getPrepaymentsAndOtherShortTermAssets().ifPresent(value -> currentAssetsMap.put("prepayments_and_other_short_term_assets", value.toString()));
                ca.getOtherReceivables().ifPresent(value -> currentAssetsMap.put("other_receivables", value.toString()));
                ca.getCryptoAssets().ifPresent(value -> currentAssetsMap.put("crypto_assets", value.toString()));
                ca.getCashAndCashEquivalents().ifPresent(value -> currentAssetsMap.put("cash_and_cash_equivalents", value.toString()));
                assetsMap.put("current_assets", currentAssetsMap);
            });

            dataMap.put("assets", assetsMap);
        });

        // Liabilities
        val liabilitiesMap = MetadataBuilder.createMap();
        balanceSheetData.getLiabilities().ifPresent(liabilities -> {
            liabilities.getNonCurrentLiabilities().ifPresent(ncl -> {
                val nonCurrentLiabilitiesMap = MetadataBuilder.createMap();
                ncl.getProvisions().ifPresent(value -> nonCurrentLiabilitiesMap.put("provisions", value.toString()));
                liabilitiesMap.put("non_current_liabilities", nonCurrentLiabilitiesMap);
            });

            liabilities.getCurrentLiabilities().ifPresent(cl -> {
                val currentLiabilitiesMap = MetadataBuilder.createMap();
                cl.getTradeAccountsPayables().ifPresent(value -> currentLiabilitiesMap.put("trade_accounts_payables", value.toString()));
                cl.getOtherCurrentLiabilities().ifPresent(value -> currentLiabilitiesMap.put("other_current_liabilities", value.toString()));
                cl.getAccrualsAndShortTermProvisions().ifPresent(value -> currentLiabilitiesMap.put("accruals_and_short_term_provisions", value.toString()));
                liabilitiesMap.put("current_liabilities", currentLiabilitiesMap);
            });

            dataMap.put("liabilities", liabilitiesMap);
        });

        // Capital
        val capitalMap = MetadataBuilder.createMap();
        balanceSheetData.getCapital().ifPresent(capital -> {
            capital.getCapital().ifPresent(value -> capitalMap.put("capital", value.toString()));
            capital.getResultsCarriedForward().ifPresent(value -> capitalMap.put("results_carried_forward", value.toString()));
            capital.getProfitForTheYear().ifPresent(value -> capitalMap.put("profit_for_the_year", value.toString()));
        });
        dataMap.put("capital", capitalMap);

        return dataMap;
    }

    private static MetadataMap serialiseIncomeStatementData(IncomeStatementData incomeStatementData) {
        val dataMap = MetadataBuilder.createMap();

        incomeStatementData.getRevenues().ifPresent(revenues -> {
            val revenuesMap = MetadataBuilder.createMap();
            revenues.getOtherIncome().ifPresent(value -> revenuesMap.put("other_income", value.toString()));
            revenues.getBuildOfLongTermProvision().ifPresent(value -> revenuesMap.put("build_of_long_term_provision", value.toString()));
            dataMap.put("revenues", revenuesMap);
        });

        incomeStatementData.getCostOfGoodsAndServices().ifPresent(cogs -> {
            val cogsMap = MetadataBuilder.createMap();
            cogs.getCostOfProvidingServices().ifPresent(value -> cogsMap.put("cost_of_providing_services", value.toString()));
            dataMap.put("cost_of_goods_and_services", cogsMap);
        });

        incomeStatementData.getOperatingExpenses().ifPresent(opex -> {
            val opexMap = MetadataBuilder.createMap();
            opex.getPersonnelExpenses().ifPresent(value -> opexMap.put("personnel_expenses", value.toString()));
            opex.getGeneralAndAdministrativeExpenses().ifPresent(value -> opexMap.put("general_and_administrative_expenses", value.toString()));
            opex.getDepreciationAndImpairmentLossesOnTangibleAssets().ifPresent(value -> opexMap.put("depreciation_and_impairment_losses_on_tangible_assets", value.toString()));
            opex.getAmortizationOnIntangibleAssets().ifPresent(value -> opexMap.put("amortization_on_intangible_assets", value.toString()));
            opex.getRentExpenses().ifPresent(value -> opexMap.put("rent_expenses", value.toString()));
            dataMap.put("operating_expenses", opexMap);
        });

        incomeStatementData.getProfitForTheYear().ifPresent(value -> dataMap.put("profit_for_the_year", value.toString()));

        return dataMap;
    }

}
