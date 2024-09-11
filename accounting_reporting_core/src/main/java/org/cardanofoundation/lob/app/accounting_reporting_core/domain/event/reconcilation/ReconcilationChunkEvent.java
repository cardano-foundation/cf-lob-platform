package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@Builder
@Getter
@DomainEvent
@NoArgsConstructor
@ToString(exclude = "transactions")
public class ReconcilationChunkEvent {

    @NotBlank
    private String reconciliationId;

    @NotBlank
    private String organisationId;

    @NotBlank
    private String adapterInstanceId;

    @NotNull
    private Integer totalTransactionsCount;

    @Size(min = 1)
    private Set<Transaction> transactions;

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

}
