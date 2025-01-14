package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.jmolecules.event.annotation.DomainEvent;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class TransactionLedgerUpdateCommand {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotBlank
    private String organisationId;

    @NotNull
    @Size(min = 1)
    private Set<Transaction> transactions;

    public static TransactionLedgerUpdateCommand create(EventMetadata metadata,
                                                        String organisationId,
                                                        Set<Transaction> txs) {
        return new TransactionLedgerUpdateCommand(metadata, organisationId, txs);
    }

}
