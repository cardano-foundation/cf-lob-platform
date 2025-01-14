package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;

import org.jmolecules.event.annotation.DomainEvent;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@DomainEvent
@Builder
public final class TxsLedgerUpdatedEvent {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotBlank
    private String organisationId;

    @NotNull
    @Size(min = 1)
    private Set<TxStatusUpdate> statusUpdates;

    public Map<String, TxStatusUpdate> statusUpdatesMap() {
        return statusUpdates.stream().collect(Collectors.toMap(TxStatusUpdate::getTxId, Function.identity()));
    }

}
