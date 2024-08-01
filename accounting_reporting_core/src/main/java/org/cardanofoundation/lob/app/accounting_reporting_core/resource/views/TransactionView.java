package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TODO: Change the Transaction class name.
 */
@Getter
@Setter
@AllArgsConstructor
public class TransactionView {

    private String id;
    private String internalTransactionNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private TransactionType transactionType;
    private ValidationStatus validationStatus = ValidationStatus.VALIDATED;
    private boolean transactionApproved = false;
    private boolean ledgerDispatchApproved = false;
    private BigDecimal amountTotalLcy;
    private Set<TransactionItemView> items = new LinkedHashSet<>();
    private Set<ViolationView> violations = new LinkedHashSet<>();
    private TransactionStatus status = TransactionStatus.OK;

}
