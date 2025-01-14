package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import java.util.Optional;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotNull;

import javax.annotation.Nullable;

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
