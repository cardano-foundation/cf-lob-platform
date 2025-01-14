package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Set;

@AllArgsConstructor
@Builder
@Getter
@DomainEvent
@NoArgsConstructor
public class TransactionBatchChunkEvent {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotBlank
    private String batchId;

    @NotBlank
    private String organisationId;

    private int totalTransactionsCount;

    @Builder.Default
    private Set<Transaction> transactions = Set.of();

    @NotNull
    private SystemExtractionParameters systemExtractionParameters;

    @Builder.Default
    private Status status = Status.STARTED;

    public enum Status {
        STARTED, PROCESSING, FINISHED
    }

}
