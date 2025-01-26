package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.IntervalType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;

@Getter
@Setter
@AllArgsConstructor
//@Builder todo: For testing
@NoArgsConstructor
@Slf4j
public class ReportSearchRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    @NotNull
    private String organisationId;

    @Schema(example = "INCOME_STATEMENT")
    @NotNull
    private ReportType reportType;

    @NotNull
    private IntervalType intervalType;

    @Schema(example = "2024")
    @NotNull
    private short year;

    @Schema(example = "3")
    @NotNull
    private short period;

}
