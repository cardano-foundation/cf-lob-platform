package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.jmolecules.event.annotation.DomainEvent;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class ScheduledIngestionEvent {

    @NotNull
    private String organisationId;

    @NotNull
    private String initiator;

    @NotNull
    private UserExtractionParameters userExtractionParameters;

}
