package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class ScheduledReconcilationEvent {

    @NotNull
    private String organisationId;

    @NotNull
    private String initiator;

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

}
