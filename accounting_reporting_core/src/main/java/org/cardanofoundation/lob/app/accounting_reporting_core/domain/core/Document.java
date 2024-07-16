package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class Document {

    @LOB_ERPSourceVersionRelevant
    @Size(min = 1, max =  255) @NotBlank private String number;

    @NotNull
    @LOB_ERPSourceVersionRelevant
    private Currency currency;

    @Builder.Default
    @NotNull
    @LOB_ERPSourceVersionRelevant
    private Optional<Vat> vat = Optional.empty();

    @Builder.Default
    @NotNull
    @LOB_ERPSourceVersionRelevant
    private Optional<Counterparty> counterparty = Optional.empty();

}
