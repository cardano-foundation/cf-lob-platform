package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode.LCY_BALANCE_MUST_BE_ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.FAILED;

import java.math.BigDecimal;
import java.util.Set;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;

class AmountLcyBalanceZerosOutCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new AmountLcyBalanceZerosOutCheckTaskItem();
    }

    @Test
    void whenLcyBalanceZerosOut_thenNoViolations() {
        val txId = Transaction.id("1", "1");
        val organisationId = "1";

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAmountLcy(new BigDecimal("100"));

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(new BigDecimal("-100"));

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setTransactionInternalNumber("1");
        tx.setOrganisation(Organisation.builder().id(organisationId).build());
        tx.setTransactionType(TransactionType.FxRevaluation);
        tx.setItems(Set.of(txItem1, txItem2));

        taskItem.run(tx);

        assertThat(tx.getViolations()).isEmpty();
    }

    @Test
    void whenLcyBalanceDoesNotZeroOut_thenViolationGenerated() {
        val txId = Transaction.id("2", "1");
        val organisationId = "1";

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "1"));
        txItem1.setAmountLcy(new BigDecimal("-99"));

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setTransactionInternalNumber("2");
        tx.setOrganisation(Organisation.builder().id(organisationId).build());
        tx.setTransactionType(TransactionType.FxRevaluation);
        tx.setItems(Set.of(txItem1));

        taskItem.run(tx);

        assertThat(tx.getAutomatedValidationStatus()).isEqualTo(FAILED);
        assertThat(tx.getViolations()).isNotEmpty();
        assertThat(tx.getViolations().size()).isEqualTo(1);
        assertThat(tx.getViolations().iterator().next().getCode()).isEqualTo(LCY_BALANCE_MUST_BE_ZERO);
    }

    @Test
    void whenNoTransactionItems_thenNoViolations() {
        val txId = Transaction.id("3", "1");
        val organisationId = "1";

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setTransactionInternalNumber("3");
        tx.setOrganisation(Organisation.builder().id(organisationId).build());
        tx.setTransactionType(TransactionType.FxRevaluation);
        tx.setItems(Set.of());

        taskItem.run(tx);

        assertThat(tx.getViolations()).isEmpty();
    }

    @Test
    void whenOnlyOneSideOfTransaction_thenViolationGenerated() {
        val txId = Transaction.id("4", "1");
        val organisationId = "1";

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "2"));
        txItem1.setAmountLcy(new BigDecimal("100"));

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setTransactionInternalNumber("4");
        tx.setOrganisation(Organisation.builder().id(organisationId).build());
        tx.setTransactionType(TransactionType.FxRevaluation);
        tx.setItems(Set.of(txItem1));

        taskItem.run(tx);

        assertThat(tx.getAutomatedValidationStatus()).isEqualTo(FAILED);
        assertThat(tx.getViolations()).isNotEmpty();
        assertThat(tx.getViolations().iterator().next().getCode()).isEqualTo(LCY_BALANCE_MUST_BE_ZERO);
    }

    @Test
    void whenLcyBalanceZerosOutWithMultipleItems_thenNoViolations() {
        val txId = Transaction.id("5", "1");
        val organisationId = "1";

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "3"));
        txItem1.setAmountLcy(new BigDecimal("50"));

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "4"));
        txItem2.setAmountLcy(new BigDecimal("30"));

        val txItem3 = new TransactionItemEntity();
        txItem3.setId(TransactionItem.id(txId, "5"));
        txItem3.setAmountLcy(new BigDecimal("-80"));

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setTransactionInternalNumber("5");
        tx.setOrganisation(Organisation.builder().id(organisationId).build());
        tx.setTransactionType(TransactionType.FxRevaluation);
        tx.setItems(Set.of(txItem1, txItem2, txItem3));

        taskItem.run(tx);

        assertThat(tx.getViolations()).isEmpty();
    }

}
