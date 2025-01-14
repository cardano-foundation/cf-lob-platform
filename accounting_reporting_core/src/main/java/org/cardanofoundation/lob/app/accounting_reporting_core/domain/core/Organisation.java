package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Organisation {

    @LOBVersionSourceRelevant
    @Size(min = 1, max =  255) @NotBlank  private String id;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> name = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> taxIdNumber = Optional.empty();

    @Builder.Default
    // ISO 3166-1 alpha-2
    private Optional<@Size(min = 2, max =  2) String> countryCode = Optional.empty();

    @Size(min = 1, max =  255)
    @NotBlank
    private String currencyId;

    @Builder.Default
    private Optional<String> adminEmail = Optional.empty();

}
