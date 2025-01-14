package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;

import org.jmolecules.event.annotation.DomainEvent;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@AllArgsConstructor
@Builder
@Getter
@DomainEvent
@NoArgsConstructor
@ToString(exclude = "transactions")
public class ReconcilationChunkEvent {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotBlank
    private String reconciliationId;

    @NotBlank
    private String organisationId;

    @NotNull
    private Integer totalTransactionsCount;

    @Size(min = 1)
    private Set<Transaction> transactions;

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

}
