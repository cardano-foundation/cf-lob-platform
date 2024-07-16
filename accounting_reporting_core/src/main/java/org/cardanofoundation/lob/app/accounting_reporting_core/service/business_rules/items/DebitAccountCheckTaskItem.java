package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.support.collections.Optionals;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class DebitAccountCheckTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getAutomatedValidationStatus() == FAILED) {
            return;
        }

        tx.getItems().removeIf(txItem -> {
            val accountDebit = txItem.getAccountDebit();
            val accountCredit = txItem.getAccountCredit();

            return Optionals.zip(accountDebit, accountCredit, (debit, credit) -> debit.getCode().equals(credit.getCode()))
                    .orElse(false);
        });
    }

}
