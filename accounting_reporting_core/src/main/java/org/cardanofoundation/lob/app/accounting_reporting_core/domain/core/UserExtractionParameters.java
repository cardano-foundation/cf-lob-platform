package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserExtractionParameters {

    @NotBlank
    private String organisationId;

    @Builder.Default
    private List<TransactionType> transactionTypes = List.of();

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

    @Builder.Default
    private List<String> transactionNumbers = List.of();

}
