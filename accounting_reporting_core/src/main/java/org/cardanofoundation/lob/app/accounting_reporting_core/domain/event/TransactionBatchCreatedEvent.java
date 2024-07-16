package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TransactionBatchCreatedEvent {

    private String batchId;
    private String organisationId;
    private String instanceId;
    private UserExtractionParameters userExtractionParameters;
    private SystemExtractionParameters systemExtractionParameters;

}
