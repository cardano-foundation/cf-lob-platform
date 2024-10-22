package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionViolation;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode.TRANSACTION_ITEMS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

@RequiredArgsConstructor
public class NoTransactionItemsTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getItems().isEmpty()) {
            tx.setAutomatedValidationStatus(FAILED); // Fail the transaction if no items are present

            handleViolationForEmptyItems(tx);
        }
    }

    private void handleViolationForEmptyItems(TransactionEntity tx) {
        val violation = TransactionViolation.builder()
                .code(TRANSACTION_ITEMS_EMPTY)
                .severity(ERROR)
                .source(ERP)
                .processorModule(NoTransactionItemsTaskItem.class.getSimpleName())
                .bag(createViolationBag(tx))
                .build();

        tx.addViolation(violation);
    }

    private Map<String, Object> createViolationBag(TransactionEntity tx) {
        return Map.of("transactionNumber", tx.getTransactionInternalNumber());
    }

}
