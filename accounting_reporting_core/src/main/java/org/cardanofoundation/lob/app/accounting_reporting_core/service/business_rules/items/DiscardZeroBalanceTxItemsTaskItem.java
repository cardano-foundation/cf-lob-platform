package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxItemValidationStatus.ERASED_ZERO_BALANCE;

public class DiscardZeroBalanceTxItemsTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        tx.getItems()
                .stream().filter(txItem -> txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() == 0)
                .forEach(txItem -> txItem.setStatus(ERASED_ZERO_BALANCE));
    }

}
