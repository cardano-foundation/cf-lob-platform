package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

public class DiscardZeroBalanceTxItemsTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        tx.getItems().removeIf(txItem -> txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() == 0);
    }

}
