package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.jmolecules.event.annotation.DomainEvent;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class ScheduledIngestionEvent {

    private String organisationId;

    private String initiator;

    private UserExtractionParameters userExtractionParameters;

}
