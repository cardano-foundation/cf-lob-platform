package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import org.springframework.modulith.events.Externalized;

import org.jmolecules.event.annotation.DomainEvent;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@AllArgsConstructor
@Builder
@DomainEvent
@Getter
@ToString
@NoArgsConstructor
@Externalized
public class TransactionBatchStartedEvent {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotBlank
    private String batchId;

    @NotBlank
    private String organisationId;

    @NotNull
    private UserExtractionParameters userExtractionParameters;

    @NotNull
    private SystemExtractionParameters systemExtractionParameters;

}
