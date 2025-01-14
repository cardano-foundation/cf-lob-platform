package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionViolation;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode.TX_TECHNICAL_FAILURE;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

@RequiredArgsConstructor
public class SanityCheckFieldsTaskItem implements PipelineTaskItem {

    private final Validator validator;

    @Override
    public void run(TransactionEntity tx) {
        val errors = validator.validate(tx);

        if (tx.getAutomatedValidationStatus() == FAILED) {
            return;
        }

        if (!errors.isEmpty()) {
            val v = TransactionViolation.builder()
                    .code(TX_TECHNICAL_FAILURE)
                    .severity(ERROR)
                    .source(LOB)
                    .processorModule(this.getClass().getSimpleName())
                    .bag(
                            Map.of(
                                    "transactionNumber", tx.getTransactionInternalNumber()
                            )
                    )
                    .build();

            tx.addViolation(v);
        }
    }

}
