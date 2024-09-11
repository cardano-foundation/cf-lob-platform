package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class Currency {

    @Nullable
    private String id;

    @LOBVersionSourceRelevant
    private String customerCode;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

}
