package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublicInterfaceTransactionsRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    @NotBlank
    private String organisationId;

    @Schema(example = "2023-01-01")
    private LocalDate dateFrom;

    @Schema(example = "2023-31-01")
    private LocalDate dateTo;

    private List<String> event;

    private String currency;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private List<String> transactionHash;
}
