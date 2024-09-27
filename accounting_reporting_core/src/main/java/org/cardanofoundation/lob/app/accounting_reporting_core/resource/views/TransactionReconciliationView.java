package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;


@Getter
@Setter
@AllArgsConstructor
public class TransactionReconciliationView {

    private String id;
    private String internalTransactionNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private TransactionType transactionType;

    private TransactionStatus status = TransactionStatus.OK;

    private LedgerDispatchStatusView statistic = LedgerDispatchStatusView.PENDING;

    private ValidationStatus validationStatus = ValidationStatus.VALIDATED;

    private boolean transactionApproved = false;

    private boolean ledgerDispatchApproved = false;

    private BigDecimal amountTotalLcy;

    private boolean itemRejection = false;

    private Set<TransactionItemView> items = new LinkedHashSet<>();

    private Set<ViolationView> violations = new LinkedHashSet<>();

    private ReconcilationCode reconcilationSource;

    private ReconcilationCode reconcilationSink;

    private ReconcilationCode reconcilationFinalStatus;

}
