package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Transaction {

    @NotBlank
    @LOB_ERPSourceVersionRelevant
    private String id;

    @LOB_ERPSourceVersionRelevant
    @Size(min = 1, max =  255) @NotBlank String internalTransactionNumber;

    @NotBlank
    private String batchId;

    @NotNull
    @LOB_ERPSourceVersionRelevant
    private LocalDate entryDate;

    @NotNull
    @LOB_ERPSourceVersionRelevant
    private TransactionType transactionType;

    @NotNull
    @LOB_ERPSourceVersionRelevant
    private Organisation organisation;

    @NotNull
    @Builder.Default
    private LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

    @NotNull
    @Builder.Default
    private ValidationStatus validationStatus = ValidationStatus.VALIDATED;

    @Builder.Default
    private boolean transactionApproved = false;

    @Builder.Default
    private boolean ledgerDispatchApproved = false;

    @NotNull
    private YearMonth accountingPeriod;

    @Builder.Default
    @NotEmpty
    private Set<TransactionItem> items = new LinkedHashSet<>();

    @Builder.Default
    private Set<Violation> violations = new LinkedHashSet<>();

    public static String id(String organisationId,
                            String internalTransactionNumber) {
        return digestAsHex(STR."\{organisationId}::\{internalTransactionNumber}");
    }

}
