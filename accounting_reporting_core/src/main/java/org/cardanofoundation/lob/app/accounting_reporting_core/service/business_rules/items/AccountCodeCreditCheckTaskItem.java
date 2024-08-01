package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.ACCOUNT_CODE_CREDIT_IS_EMPTY;

@RequiredArgsConstructor
public class AccountCodeCreditCheckTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getTransactionType() == Journal) {
            return;
        }

        for (val txItem : tx.getItems()) {
            if (txItem.getAccountCredit().map(Account::getCode).map(String::trim).filter(a -> !a.isEmpty()).isEmpty()) {
                val v = Violation.builder()
                        .code(ACCOUNT_CODE_CREDIT_IS_EMPTY)
                        .txItemId(txItem.getId())
                        .severity(ERROR)
                        .source(ERP)
                        .processorModule(this.getClass().getSimpleName())
                        .bag(Map.of("transactionNumber", tx.getTransactionInternalNumber()))
                        .build();

                tx.addViolation(v);
            }
        }
    }

}