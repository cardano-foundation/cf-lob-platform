package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SystemExtractionParameters {

    @NotBlank
    private String organisationId;

    @NotNull
    private LocalDate accountPeriodFrom;

    @NotNull
    private LocalDate accountPeriodTo;

}
