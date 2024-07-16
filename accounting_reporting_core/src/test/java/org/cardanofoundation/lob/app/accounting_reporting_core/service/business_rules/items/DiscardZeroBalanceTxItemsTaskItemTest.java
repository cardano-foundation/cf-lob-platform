package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;

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

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(BigDecimal.valueOf(200));
        txItem2.setAmountFcy(BigDecimal.valueOf(0));

        val txItem3 = new TransactionItemEntity();
        txItem3.setId(TransactionItem.id(txId, "2"));
        txItem3.setAmountLcy(BigDecimal.valueOf(300));
        txItem3.setAmountFcy(BigDecimal.valueOf(300));

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
    }

    @Test
    public void testDiscardTxItemsWithZeroBalance() {
        val txId = Transaction.id("1", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAmountLcy(BigDecimal.valueOf(0));
        txItem1.setAmountFcy(BigDecimal.valueOf(0));

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(BigDecimal.valueOf(200));
        txItem2.setAmountFcy(BigDecimal.valueOf(200));

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(1);
        assertThat(tx.getItems().stream().map(TransactionItemEntity::getAmountLcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(200));
        assertThat(tx.getItems().stream().map(TransactionItemEntity::getAmountFcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(200));
    }

    @Test
    void testDiscardAllTxItemsWithZeroBalance() {
        val txId = Transaction.id("2", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAmountLcy(BigDecimal.ZERO);
        txItem1.setAmountFcy(BigDecimal.ZERO);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAmountLcy(BigDecimal.ZERO);
        txItem2.setAmountFcy(BigDecimal.ZERO);

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).isEmpty();
    }

}
