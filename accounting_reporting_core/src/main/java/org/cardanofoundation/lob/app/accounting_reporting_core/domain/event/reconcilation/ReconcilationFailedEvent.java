package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError;
import org.jmolecules.event.annotation.DomainEvent;

@AllArgsConstructor
@Builder
@DomainEvent
@Getter
@ToString
@NoArgsConstructor
public class ReconcilationFailedEvent {

    @NotBlank
    private String reconciliationId;

    @NotBlank
    private String organisationId;

    @NotBlank
    private String adapterInstanceId;

    @NotNull
    private FatalError error;

}
