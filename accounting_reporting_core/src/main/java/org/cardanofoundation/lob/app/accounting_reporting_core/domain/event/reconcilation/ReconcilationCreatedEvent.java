package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import lombok.*;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDate;

@DomainEvent
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class ReconcilationCreatedEvent {

    private String reconciliationId;
    private String organisationId;
    private String adapterInstanceId;
    private LocalDate from;
    private LocalDate to;

}
