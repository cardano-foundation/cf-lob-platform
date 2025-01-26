package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxItemValidationStatus.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;

public class DiscardZeroBalanceTxItemsTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new DiscardZeroBalanceTxItemsTaskItem();
    }

    @Test
    public void testNoDiscard() {
        val txId = Transaction.id("1", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAmountLcy(BigDecimal.valueOf(0));
        txItem1.setAmountFcy(BigDecimal.valueOf(100));
        txItem1.setStatus(OK);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(BigDecimal.valueOf(200));
        txItem2.setAmountFcy(BigDecimal.valueOf(0));
        txItem2.setStatus(OK);

        val txItem3 = new TransactionItemEntity();
        txItem3.setId(TransactionItem.id(txId, "2"));
        txItem3.setAmountLcy(BigDecimal.valueOf(300));
        txItem3.setAmountFcy(BigDecimal.valueOf(300));
        txItem3.setStatus(OK);

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);
        txItems.add(txItem3);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(3);
        assertThat(tx.getItems().stream().map(TransactionItemEntity::getAmountLcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(0), BigDecimal.valueOf(200), BigDecimal.valueOf(300));
        assertThat(tx.getItems().stream().map(TransactionItemEntity::getAmountFcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(0), BigDecimal.valueOf(300));

        // Ensure all non-zero balance items have status OK
        assertThat(tx.getItems().stream().allMatch(item -> item.getStatus() == OK)).isTrue();
    }

    @Test
    public void testDiscardTxItemsWithZeroBalance() {
        val txId = Transaction.id("1", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAmountLcy(BigDecimal.valueOf(0));
        txItem1.setAmountFcy(BigDecimal.valueOf(0));
        txItem1.setStatus(OK);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(BigDecimal.valueOf(200));
        txItem2.setAmountFcy(BigDecimal.valueOf(200));
        txItem2.setStatus(OK);

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        // Check that the zero-balance item is marked as ERASED
        assertThat(txItem1.getStatus()).isEqualTo(ERASED_ZERO_BALANCE);
        // Check that the non-zero balance item remains OK
        assertThat(txItem2.getStatus()).isEqualTo(OK);
    }

    @Test
    void testDiscardAllTxItemsWithZeroBalance() {
        val txId = Transaction.id("2", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAmountLcy(BigDecimal.ZERO);
        txItem1.setAmountFcy(BigDecimal.ZERO);
        txItem1.setStatus(OK);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(BigDecimal.ZERO);
        txItem2.setAmountFcy(BigDecimal.ZERO);
        txItem2.setStatus(OK);

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        // Ensure all zero-balance items are marked as ERASED
        assertThat(tx.getItems().stream().allMatch(item -> item.getStatus() == ERASED_ZERO_BALANCE)).isTrue();
    }

    @Test
    void testNoItemsToDiscard() {
        val tx = new TransactionEntity();
        tx.setItems(new LinkedHashSet<>());

        taskItem.run(tx);

        // Ensure no errors occur and the item list remains empty
        assertThat(tx.getItems()).isEmpty();
    }

}
