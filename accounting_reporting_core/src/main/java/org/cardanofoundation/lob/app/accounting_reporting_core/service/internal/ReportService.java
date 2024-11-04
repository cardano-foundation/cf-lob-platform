package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports.BalanceSheetData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reports.IncomeStatementData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportMode.USER;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportRollupPeriodType.MONTHLY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportRollupPeriodType.YEARLY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportType.BALANCE_SHEET;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportType.INCOME_STATEMENT;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final OrganisationPublicApi organisationPublicApi;
    private final Clock clock;

    @Transactional
    public Either<Problem, Void> approveReport(String reportId) {
        val reportM = reportRepository.findById(reportId);

        if (reportM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("REPORT_NOT_FOUND")
                    .withDetail(STR."Report with ID \{reportId} does not exist.")
                    .with("reportId", reportId)
                    .build());
        }
        val report = reportM.orElseThrow();
        report.setReportApproved(true);

        reportRepository.save(report);

        return Either.right(null);
    }

    @Transactional
    public Either<Problem, Void> approveReportForLedgerDispatch(String reportId) {
        val reportM = reportRepository.findById(reportId);

        if (reportM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("REPORT_NOT_FOUND")
                    .withDetail(STR."Report with ID \{reportId} does not exist.")
                    .with("reportId", reportId)
                    .build());
        }
        val report = reportM.orElseThrow();
        report.setLedgerDispatchApproved(true);

        reportRepository.save(report);

        return Either.right(null);
    }

    public boolean exists(String reportId) {
        return reportRepository.existsById(reportId);
    }

    public Optional<ReportEntity> findById(String reportId) {
        return reportRepository.findById(reportId);
    }

    @Transactional
    public Either<Problem, Void> storeIncomeStatement(String organisationId) {
        log.info("Income Statement::Saving report example...");

        val orgM = organisationPublicApi.findByOrganisationId(organisationId);
        if (orgM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Organisation with ID \{organisationId} does not exist.")
                    .with("organisationId", organisationId)
                    .build());
        }
        val org = orgM.orElseThrow();

        val reportExample = new ReportEntity();
        reportExample.setReportId(ReportEntity.id(organisationId, INCOME_STATEMENT, MONTHLY, (short) 2023, Optional.of((short) 3)));

        reportExample.setOrganisation(Organisation.builder()
                .id(organisationId)
                .countryCode(org.getCountryCode())
                .name(org.getName())
                .taxIdNumber(org.getTaxIdNumber())
                .currencyId(org.getCurrencyId())
                .build()
        );

        reportExample.setType(INCOME_STATEMENT);
        reportExample.setRollupPeriod(MONTHLY); // Assuming MONTHLY is a constant in ReportRollupPeriodType
        reportExample.setYear((short) 2023);
        reportExample.setPeriod((short) 3); // Representing March
        reportExample.setMode(USER); // Assuming USER is a constant in ReportMode enum
        reportExample.setDate(LocalDate.now(clock));

        val incomeStatementReportData = IncomeStatementData.builder()
                .revenues(IncomeStatementData.Revenues.builder()
                        .otherIncome(new BigDecimal("10000.90"))
                        .buildOfLongTermProvision(new BigDecimal("1000000.10"))
                        .build())
                .cogs(IncomeStatementData.COGS.builder()
                        .costOfProvidingServices(new BigDecimal("500000.15"))
                        .build())
                .operatingProfit(IncomeStatementData.OperatingProfit.builder()
                        .financeIncome(new BigDecimal("200000.53"))
                        .netIncomeOptionsSale(new BigDecimal("100000.10"))
                        .realisedGainsOnSaleOfCryptocurrencies(new BigDecimal("50000.15"))
                        .stakingRewardsIncome(new BigDecimal("10000.53"))
                        .financeExpenses(new BigDecimal("20000.10"))
                        .extraordinaryExpenses(new BigDecimal("1000.15"))
                        .build())
                .taxExpenses(IncomeStatementData.TaxExpenses.builder()
                        .incomeTaxExpense(new BigDecimal("1000.51"))
                        .build())
                .operatingExpenses(IncomeStatementData.OperatingExpenses.builder()
                        .personnelExpenses(new BigDecimal("500000.15"))
                        .generalAndAdministrativeExpenses(new BigDecimal("200000.53"))
                        .build())
                .build();

        reportExample.setIncomeStatementReportData(Optional.of(incomeStatementReportData));

        reportRepository.save(reportExample);

        log.info("Income Statement::Report saved successfully: {}", reportExample);

        return Either.right(null);
    }

    @Transactional
    public Either<Problem, Void> storeBalanceSheet(String organisationId) {
        log.info("Balance Sheet:: Saving report...");

        val orgM = organisationPublicApi.findByOrganisationId(organisationId);
        if (orgM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Organisation with ID \{organisationId} does not exist.")
                    .with("organisationId", organisationId)
                    .build());
        }
        val org = orgM.orElseThrow();

        val reportExample = new ReportEntity();
        reportExample.setReportId(ReportEntity.id(organisationId, INCOME_STATEMENT, YEARLY, (short) 2024, Optional.empty()));

        reportExample.setOrganisation(Organisation.builder()
                .id(organisationId)
                .countryCode(org.getCountryCode())
                .name(org.getName())
                .taxIdNumber(org.getTaxIdNumber())
                .currencyId(org.getCurrencyId())
                .build()
        );

        reportExample.setType(BALANCE_SHEET);
        reportExample.setRollupPeriod(MONTHLY); // Assuming MONTHLY is a constant in ReportRollupPeriodType
        reportExample.setYear((short) 2023);
        reportExample.setPeriod((short) 3); // Representing March
        reportExample.setMode(USER); // Assuming USER is a constant in ReportMode enum
        reportExample.setDate(LocalDate.now(clock));

        val balanceSheetReportData = BalanceSheetData.builder()
                .assets(BalanceSheetData.Assets.builder()
                        .nonCurrentAssets(BalanceSheetData.Assets.NonCurrentAssets.builder()
                                .propertyPlantEquipment(new BigDecimal("500000.00"))
                                .intangibleAssets(new BigDecimal("120000.00"))
                                .investments(new BigDecimal("200000.00"))
                                .financialAssets(new BigDecimal("150000.00"))
                                .build())
                        .currentAssets(BalanceSheetData.Assets.CurrentAssets.builder()
                                .prepaymentsAndOtherShortTermAssets(new BigDecimal("30000.00"))
                                .otherReceivables(new BigDecimal("50000.00"))
                                .cryptoAssets(new BigDecimal("100000.00"))
                                .cashAndCashEquivalents(new BigDecimal("75000.00"))
                                .build())
                        .build())
                .liabilities(BalanceSheetData.Liabilities.builder()
                        .nonCurrentLiabilities(BalanceSheetData.Liabilities.NonCurrentLiabilities.builder()
                                .provisions(new BigDecimal("20000.00"))
                                .build())
                        .currentLiabilities(BalanceSheetData.Liabilities.CurrentLiabilities.builder()
                                .tradeAccountsPayables(new BigDecimal("15000.00"))
                                .otherCurrentLiabilities(new BigDecimal("10000.00"))
                                .accrualsAndShortTermProvisions(new BigDecimal("5000.00"))
                                .build())
                        .build())
                .capital(BalanceSheetData.Capital.builder()
                        .capital(new BigDecimal("300000.00"))
                        .retainedEarnings(new BigDecimal("200000.00"))
                        .freeFoundationCapital(new BigDecimal("100000.00"))
                        .build())
                .build();

        reportExample.setBalanceSheetReportData(Optional.of(balanceSheetReportData));

        if (!reportExample.isValid()) {
            return Either.left(Problem.builder()
                    .withTitle("INVALID_REPORT")
                    .withDetail(STR."Report is not valid since it didn't pass through business checks.")
                    .with("reportId", reportExample.getReportId())
                    .with("reportType", reportExample.getType())
                    .build());
        }

        reportRepository.save(reportExample);

        log.info("Balance Sheet::Report saved successfully: {}", reportExample.getReportId());

        return Either.right(null);
    }

    public Either<Problem, Boolean> isReportValid(String reportId) {
        val reportM = reportRepository.findById(reportId);

        if (reportM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("REPORT_NOT_FOUND")
                    .withDetail(STR."Report with ID \{reportId} does not exist.")
                    .with("reportId", reportId)
                    .build());
        }

        val report = reportM.orElseThrow();

        return Either.right(report.isValid());
    }

}
