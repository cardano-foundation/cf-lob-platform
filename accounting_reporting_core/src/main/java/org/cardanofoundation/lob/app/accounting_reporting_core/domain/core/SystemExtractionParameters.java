package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.YearMonth;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SystemExtractionParameters {

    @NotBlank
    private String organisationId;

    @NotNull
    private YearMonth accountPeriodFrom;

    @NotNull
    private YearMonth accountPeriodTo;

}
