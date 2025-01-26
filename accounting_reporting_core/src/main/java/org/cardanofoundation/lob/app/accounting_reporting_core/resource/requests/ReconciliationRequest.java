package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
//@Builder todo: For testing
@NoArgsConstructor
@Slf4j
public class ReconciliationRequest {
    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    private String organisationId;

    @Schema(example = "2014-01-01")
    private LocalDate dateFrom;

    @Schema(example = "2024-07-31")
    private LocalDate dateTo;
    }
