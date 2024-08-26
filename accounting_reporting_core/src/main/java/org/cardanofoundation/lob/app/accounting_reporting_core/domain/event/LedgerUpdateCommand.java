package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class LedgerUpdateCommand {

    private String organisationId;
    private Set<Transaction> transactions;

    public static LedgerUpdateCommand create(String organisationId,
                                             Set<Transaction> txs) {
        return new LedgerUpdateCommand(organisationId, txs);
    }

}
