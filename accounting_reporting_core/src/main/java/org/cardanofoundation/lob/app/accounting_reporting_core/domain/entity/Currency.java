package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import java.util.Optional;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;

import lombok.*;

import org.hibernate.envers.Audited;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
@Audited
public class Currency {

    @Nullable
    private String id;

    @LOBVersionSourceRelevant
    private String customerCode;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

}
