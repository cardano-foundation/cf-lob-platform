package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationRejectionCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class TransactionView {

    private String id;
    private String internalTransactionNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private TransactionType transactionType;

    private TransactionStatus status = TransactionStatus.OK;

    private LedgerDispatchStatusView statistic = LedgerDispatchStatusView.PENDING;

    private TxValidationStatus validationStatus = TxValidationStatus.VALIDATED;

    private boolean transactionApproved = false;

    private boolean ledgerDispatchApproved = false;

    private BigDecimal amountTotalLcy;

    private boolean itemRejection = false;

    private ReconciliationCodeView reconciliationSource = ReconciliationCodeView.NEVER;

    private ReconciliationCodeView reconciliationSink = ReconciliationCodeView.NEVER;

    private ReconciliationCodeView reconciliationFinalStatus = ReconciliationCodeView.NEVER;

    private Set<ReconcilationRejectionCode> reconciliationRejectionCode = new LinkedHashSet<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reconciliationDate;

    private Set<TransactionItemView> items = new LinkedHashSet<>();

    private Set<ViolationView> violations = new LinkedHashSet<>();

    public enum ReconciliationCodeView {
        OK,
        NOK,
        NEVER;

        public static ReconciliationCodeView of(ReconcilationCode code) {
            return switch (code) {
                case OK -> ReconciliationCodeView.OK;
                case NOK -> ReconciliationCodeView.NOK;
            };
        }
    }

}
