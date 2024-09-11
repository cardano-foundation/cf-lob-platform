package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.jmolecules.event.annotation.DomainEvent;

@AllArgsConstructor
@Builder
@DomainEvent
@Getter
@ToString
public class TransactionBatchStartedEvent {

    @NotBlank
    private String batchId;

    @NotBlank
    private String organisationId;

    @NotBlank
    private String initiator;

    @NotBlank
    private String adapterInstanceId;

    @NotNull
    private UserExtractionParameters userExtractionParameters;

    @NotNull
    private SystemExtractionParameters systemExtractionParameters;

}
