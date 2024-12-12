package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@Embeddable
public class Document {

    private String num;

    @Embedded
    private Currency currency;

    @Embedded
    @Nullable
    private Vat vat;

    @Nullable
    @Embedded
    private Counterparty counterparty;

    public Optional<Vat> getVat() {
        return Optional.ofNullable(vat);
    }

    public Optional<Counterparty> getCounterparty() {
        return Optional.ofNullable(counterparty);
    }

    public void setCounterparty(Optional<Counterparty> counterparty) {
        this.counterparty = counterparty.orElse(null);
    }

}
