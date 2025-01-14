package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Currency {

    @NotBlank
    @LOBVersionSourceRelevant
    private String customerCode;

    @Builder.Default
    private Optional<CoreCurrency> coreCurrency = Optional.empty();

}
