package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
@Audited
public class Document {

    @LOBVersionSourceRelevant
    private String num;

    @Embedded
    @NotNull
    @LOBVersionSourceRelevant
    private Currency currency;

    @Embedded
    @Nullable
    @LOBVersionSourceRelevant
    private Vat vat;

    @Embedded
    @Nullable
    @LOBVersionSourceRelevant
    private Counterparty counterparty;

    public Optional<Vat> getVat() {
        return Optional.ofNullable(vat);
    }

    public Optional<Counterparty> getCounterparty() {
        return Optional.ofNullable(counterparty);
    }

}
