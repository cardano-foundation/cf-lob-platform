package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;
import org.javers.core.metamodel.annotation.DiffInclude;

import javax.annotation.Nullable;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class Counterparty {

    @LOBVersionSourceRelevant
    
    private String customerCode;

    @Enumerated(STRING)
    @LOBVersionSourceRelevant
    
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type type;

    @Nullable
    private String name;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

}
