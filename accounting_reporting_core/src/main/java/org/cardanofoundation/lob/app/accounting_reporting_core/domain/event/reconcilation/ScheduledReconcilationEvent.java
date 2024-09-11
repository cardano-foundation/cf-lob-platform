package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@DomainEvent
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
