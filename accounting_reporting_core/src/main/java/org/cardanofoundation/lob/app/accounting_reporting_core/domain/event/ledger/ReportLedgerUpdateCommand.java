package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.Report;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Set;

/**
 * API3 / Module3 command to update the ledger with a set of reports, typically send to the blockchain publisher
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class ReportLedgerUpdateCommand {

    public static final String VERSION = "1.0";

    @NotNull
    private EventMetadata metadata;

    @NotBlank
    private String organisationId;

    @NotNull
    @Size(min = 1)
    private Set<Report> reports; // report canonical model

    public static ReportLedgerUpdateCommand create(EventMetadata metadata,
                                                   String organisationId,
                                                   Set<Report> reports) {
        return new ReportLedgerUpdateCommand(metadata, organisationId, reports);
    }

}
