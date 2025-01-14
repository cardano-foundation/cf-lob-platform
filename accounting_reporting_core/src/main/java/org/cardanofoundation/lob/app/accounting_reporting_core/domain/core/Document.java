package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class Document {

    @LOBVersionSourceRelevant
    @Size(min = 1, max =  255) @NotBlank private String number;

    @NotNull
    private Currency currency;

    @Builder.Default
    @NotNull
    private Optional<Vat> vat = Optional.empty();

    @Builder.Default
    @NotNull
    private Optional<Counterparty> counterparty = Optional.empty();

}
