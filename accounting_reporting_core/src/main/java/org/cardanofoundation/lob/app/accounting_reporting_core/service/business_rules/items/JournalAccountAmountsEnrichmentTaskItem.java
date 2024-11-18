package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.CREDIT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;

@Slf4j
public class JournalAccountAmountsEnrichmentTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getTransactionType() != Journal) {
            return;
        }

        for (val txItem : tx.getItems()) {
            if (txItem.getOperationType().equals(Optional.of(CREDIT))){
                txItem.setAmountFcy(txItem.getAmountFcy().abs().negate());
                txItem.setAmountLcy(txItem.getAmountLcy().abs().negate());
                continue;
            }
            txItem.setAmountFcy(txItem.getAmountFcy().abs());
            txItem.setAmountLcy(txItem.getAmountLcy().abs());
        }
    }

}
