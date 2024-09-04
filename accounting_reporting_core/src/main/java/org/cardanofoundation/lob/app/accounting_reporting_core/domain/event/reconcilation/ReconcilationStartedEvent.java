package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@DomainEvent
@Getter
@ToString
@NoArgsConstructor
public class ReconcilationStartedEvent {

    @NotBlank
    private String reconciliationId;

    @NotBlank
    private String organisationId;

    @NotBlank
    private String adapterInstanceId;

    @NotNull
    private String initiator;

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

}
