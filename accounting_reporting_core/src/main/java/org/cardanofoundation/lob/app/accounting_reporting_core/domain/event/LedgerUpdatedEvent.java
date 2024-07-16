package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@DomainEvent
public final class LedgerUpdatedEvent {

    private String organisationId;
    private Set<TxStatusUpdate> statusUpdates;

    public Map<String, TxStatusUpdate> statusUpdatesMap() {
        return statusUpdates.stream().collect(Collectors.toMap(TxStatusUpdate::getTxId, Function.identity()));
    }

}
