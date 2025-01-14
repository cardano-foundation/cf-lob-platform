package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ReportPublishRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    @NotBlank
    private String organisationId;

    @Schema(example = "25acd91f465974740dc89f9f0f428235773c2385bb81eeca379bb821c86e089f")
    @NotBlank
    private String reportId;
}
