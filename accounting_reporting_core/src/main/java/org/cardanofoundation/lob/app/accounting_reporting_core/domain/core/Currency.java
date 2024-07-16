package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Currency {

    @NotBlank
    @LOB_ERPSourceVersionRelevant
    private String customerCode;

    @Builder.Default
    private Optional<CoreCurrency> coreCurrency = Optional.empty();

}
