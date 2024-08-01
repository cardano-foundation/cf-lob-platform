package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.DOCUMENT_MUST_BE_PRESENT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

@RequiredArgsConstructor
public class DocumentMustBePresentTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        for (val txItem : tx.getItems()) {
            if (txItem.getDocument().isEmpty()) {
                val v = Violation.builder()
                        .txItemId(txItem.getId())
                        .code(DOCUMENT_MUST_BE_PRESENT)
                        .severity(ERROR)
                        .source(ERP)
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

}