package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;

import java.time.LocalDate;
import java.util.Optional;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Report {

    private String reportId;

    private Organisation organisation;

    private ReportType type;

    private ReportRollupPeriodType rollupPeriod;

    private short year;

    @Builder.Default
    private Optional<Short> period = Optional.empty();

    @Builder.Default
    private ReportMode mode = ReportMode.USER;

    private LocalDate date;

    @Builder.Default
    private Optional<BalanceSheetData> balanceSheetData = Optional.empty();

    @Builder.Default
    private Optional<IncomeStatementData> incomeStatementData = Optional.empty();

    @Builder.Default
    private boolean reportApproved = false;

    @Builder.Default
    private boolean ledgerDispatchApproved = false;

    @Builder.Default
    private LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

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

    public void setBalanceSheetData(Optional<BalanceSheetData> balanceSheetData) {
        if (type != ReportType.BALANCE_SHEET) {
            throw new IllegalArgumentException("Balance sheet data can only be set for balance sheet report type!");
        }

        this.balanceSheetData = balanceSheetData;
    }

    public void setIncomeStatementData(Optional<IncomeStatementData> incomeStatementData) {
        if (type != ReportType.INCOME_STATEMENT) {
            throw new IllegalArgumentException("Income statement data can only be set for income statement report type!");
        }

        this.incomeStatementData = incomeStatementData;
    }

}