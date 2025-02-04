package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import lombok.*;

import org.springframework.modulith.events.Externalized;

import org.jmolecules.event.annotation.DomainEvent;

import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@DomainEvent
@Externalized
public class ScheduledReconcilationEvent {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotNull
    private String organisationId;

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

}
