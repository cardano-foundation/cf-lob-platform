package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.CREDIT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionViolation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;

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

        Optional<String> dummyAccountM = organisationPublicApiIF.findByOrganisationId(tx.getOrganisation().getId())
                .flatMap(Organisation::getDummyAccount);

        if (!shouldTriggerNormalisation(tx, dummyAccountM)) {
            if (dummyAccountM.isEmpty()) {
                TransactionViolation v = TransactionViolation.builder()
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
        String dummyAccount = dummyAccountM.orElseThrow();
        for (TransactionItemEntity txItem : tx.getItems()) {
            OperationType operationType = txItem.getOperationType();

            if (txItem.getAccountCredit().isEmpty() && operationType == CREDIT) {
                Account accountDebit = txItem.getAccountDebit().orElseThrow();
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
