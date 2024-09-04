package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionViolation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.CREDIT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

@RequiredArgsConstructor
@Slf4j
public class JournalAccountCreditEnrichmentTaskItem implements PipelineTaskItem {

    public static final String DUMMY_ACCOUNT = "Dummy Account";

    private final OrganisationPublicApiIF organisationPublicApiIF;

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getTransactionType() != Journal) {
            return;
        }

        val dummyAccountM = organisationPublicApiIF.findByOrganisationId(tx.getOrganisation().getId())
                .flatMap(Organisation::getDummyAccount);

        if (!shouldTriggerNormalisation(tx, dummyAccountM)) {
            if (dummyAccountM.isEmpty()) {
                val v = TransactionViolation.builder()
                        .code(TransactionViolationCode.JOURNAL_DUMMY_ACCOUNT_MISSING)
                        .processorModule(this.getClass().getSimpleName())
                        .source(LOB)
                        .severity(ERROR)
                        .build();
                tx.addViolation(v);
            }

            return;
        }

        log.info("Normalising journal transaction with id: {}", tx.getId());

        // at this point we can assume we have it, it is mandatory
        val dummyAccount = dummyAccountM.orElseThrow();
        for (val txItem : tx.getItems()) {
            val operationTypeM = txItem.getOperationType();

            if (operationTypeM.isEmpty()) {
                txItem.setAccountCredit(Optional.of(Account.builder()
                        .code(dummyAccount)
                        .name(DUMMY_ACCOUNT)
                        .build()));
                continue;
            }

            val operationType = operationTypeM.orElseThrow();

            if (txItem.getAccountCredit().isEmpty() && operationType == CREDIT) {
                val accountDebit = txItem.getAccountDebit().orElseThrow();
                txItem.setAccountCredit(Optional.of(accountDebit));

                txItem.clearAccountCodeDebit();
            }

            if (txItem.getAccountCredit().isEmpty()) {
                txItem.setAccountCredit(Optional.of(Account.builder()
                        .code(dummyAccount)
                        .name(DUMMY_ACCOUNT)
                        .build()));
            }

            if (txItem.getAccountDebit().isEmpty()) {
                txItem.setAccountDebit(Optional.of(Account.builder()
                        .code(dummyAccount)
                        .name(DUMMY_ACCOUNT)
                        .build()));
            }
        }
    }

    private boolean shouldTriggerNormalisation(TransactionEntity tx,
                                               Optional<String> dummyAccountM) {
        return dummyAccountM.isPresent() && isEmptyAccountCreditsInJournalTx(tx);
    }

    private boolean isEmptyAccountCreditsInJournalTx(TransactionEntity tx) {
        return tx.getTransactionType() == Journal
                && tx.getItems().stream().allMatch(txItem -> txItem.getAccountCredit().isEmpty());
    }

}
