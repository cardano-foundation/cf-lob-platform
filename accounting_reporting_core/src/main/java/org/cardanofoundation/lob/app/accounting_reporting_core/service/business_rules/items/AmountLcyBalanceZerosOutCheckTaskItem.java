package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;


import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.LCY_BALANCE_MUST_BE_ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

@RequiredArgsConstructor
public class AmountLcyBalanceZerosOutCheckTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        val txItems = tx.getItems();

        val lcySum = txItems.stream()
                .map(TransactionItemEntity::getAmountLcy)
                .reduce(ZERO, BigDecimal::add);

        if (lcySum.signum() != 0) {
            val v = Violation.builder()
                    .code(LCY_BALANCE_MUST_BE_ZERO)
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
