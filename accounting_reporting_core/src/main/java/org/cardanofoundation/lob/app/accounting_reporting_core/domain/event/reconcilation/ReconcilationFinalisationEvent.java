package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.event.annotation.DomainEvent;

@AllArgsConstructor
@Builder
@Getter
@DomainEvent
@NoArgsConstructor
public class ReconcilationFinalisationEvent {

    private String reconciliationId;
    private String organisationId;
    private String adapterInstanceId;

}
