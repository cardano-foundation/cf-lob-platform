package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Project {

    @LOB_ERPSourceVersionRelevant
    private @Size(min = 1, max =  255) String customerCode;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> externalCustomerCode = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> name = Optional.empty();

}
