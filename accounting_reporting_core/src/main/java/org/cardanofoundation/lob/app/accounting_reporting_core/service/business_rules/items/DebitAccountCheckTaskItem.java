package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.support.collections.Optionals;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxItemValidationStatus.ERASED_SELF_PAYMENT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.FAILED;

@Slf4j
public class DebitAccountCheckTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getAutomatedValidationStatus() == FAILED) {
            return;
        }

        tx.getItems().stream().filter(txItem -> {
            val accountDebit = txItem.getAccountDebit();
            val accountCredit = txItem.getAccountCredit();
            /**
             * TODO: For Journals. Need the dummy account constant to use it in here.
             */
            return Optionals.zip(accountDebit, accountCredit, (debit, credit) -> debit.getCode().equals(credit.getCode()) || debit.getCode().equals("0000000000"))
                    .orElse(false);
        }).forEach(txItem -> txItem.setStatus(ERASED_SELF_PAYMENT));
    }

}
