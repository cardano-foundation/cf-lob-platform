package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;
import org.javers.core.metamodel.annotation.DiffInclude;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Account {

    @LOBVersionSourceRelevant
    
    private String code;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> name = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> refCode = Optional.empty();

}
