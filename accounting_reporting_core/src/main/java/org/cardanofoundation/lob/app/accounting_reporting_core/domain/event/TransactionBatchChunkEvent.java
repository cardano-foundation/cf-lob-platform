package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Builder
@Getter
@DomainEvent
@NoArgsConstructor
public class TransactionBatchChunkEvent {

    private String batchId;

    private String organisationId;

    @Builder.Default
    private Optional<Integer> totalTransactionsCount = Optional.empty();

    @Builder.Default
    private Set<Transaction> transactions = Set.of();

    private SystemExtractionParameters systemExtractionParameters;

    @Builder.Default
    private Status status = Status.STARTED;

    public enum Status {
        STARTED, PROCESSING, FINISHED
    }

}
