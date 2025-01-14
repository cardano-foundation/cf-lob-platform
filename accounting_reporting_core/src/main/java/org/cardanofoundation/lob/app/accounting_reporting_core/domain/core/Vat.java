package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.math.BigDecimal;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Vat {

    @LOBVersionSourceRelevant
    @Size(min = 1, max =  255) @NotBlank private String customerCode;

    @Builder.Default
    private Optional<BigDecimal> rate = Optional.empty(); // needed for blockchain data conversion

}
