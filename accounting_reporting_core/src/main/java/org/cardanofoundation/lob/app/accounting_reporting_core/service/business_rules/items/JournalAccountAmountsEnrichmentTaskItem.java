package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;

@Slf4j
public class JournalAccountAmountsEnrichmentTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getTransactionType() != Journal) {
            return;
        }

        for (val txItem : tx.getItems()) {
            txItem.setAmountFcy(txItem.getAmountFcy().abs());
            txItem.setAmountLcy(txItem.getAmountLcy().abs());
        }
    }

}
