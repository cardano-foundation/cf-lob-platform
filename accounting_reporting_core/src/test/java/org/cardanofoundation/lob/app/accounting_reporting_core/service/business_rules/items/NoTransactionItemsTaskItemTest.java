package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode.TRANSACTION_ITEMS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;

class NoTransactionItemsTaskItemTest {

    private NoTransactionItemsTaskItem taskItem;
    private TransactionEntity transaction;

    @BeforeEach
    void setUp() {
        taskItem = new NoTransactionItemsTaskItem();
        transaction = new TransactionEntity();
        transaction.setTransactionInternalNumber("TX-123");
    }

    @Test
    void shouldFailTransactionWhenNoItemsPresent() {
        // Given: An empty set of transaction items
        transaction.setItems(Set.of());

        // When: The task item is run
        taskItem.run(transaction);

        // Then: The transaction should have FAILED validation status
        assertThat(transaction.getAutomatedValidationStatus()).isEqualTo(FAILED);

        // And: A violation should be added to the transaction
        assertThat(transaction.getViolations()).hasSize(1);

        // And: The violation should have the correct properties
        TransactionViolation violation = transaction.getViolations().iterator().next();
        assertThat(violation.getCode()).isEqualTo(TRANSACTION_ITEMS_EMPTY);
        assertThat(violation.getSeverity()).isEqualTo(ERROR);
        assertThat(violation.getSource()).isEqualTo(ERP);
        assertThat(violation.getProcessorModule()).isEqualTo(NoTransactionItemsTaskItem.class.getSimpleName());
        assertThat(violation.getBag()).containsEntry("transactionNumber", "TX-123");
    }

    @Test
    void shouldNotAddViolationWhenTransactionItemsArePresent() {
        // Given: A set of transaction items
        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1:0");

        transaction.setItems(Set.of(txItem1)); // Mock or stub transaction items

        // When: The task item is run
        taskItem.run(transaction);

        // Then: The transaction should not have a FAILED validation status
        assertThat(transaction.getAutomatedValidationStatus()).isNotEqualTo(FAILED);

        // And: No violation should be added
        assertThat(transaction.getViolations()).isEmpty();
    }

    @Test
    void shouldNotDuplicateViolationIfRunMultipleTimes() {
        // Given: An empty set of transaction items
        transaction.setItems(Set.of());

        // When: The task item is run multiple times
        taskItem.run(transaction);
        taskItem.run(transaction); // Running again

        // Then: Only one violation should be present, even after multiple runs
        assertThat(transaction.getViolations()).hasSize(1);
    }

    @Test
    void shouldNotOverrideExistingFailedStatus() {
        // Given: A transaction that has already failed for a different reason
        transaction.setAutomatedValidationStatus(FAILED);
        transaction.setItems(Set.of());

        // When: The task item is run
        taskItem.run(transaction);

        // Then: The validation status should remain FAILED
        assertThat(transaction.getAutomatedValidationStatus()).isEqualTo(FAILED);

        // And: No additional violation should be added for the same failure
        assertThat(transaction.getViolations()).hasSize(1);
    }

}