package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.AccountEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

class TxItemsCollapsingTaskItemTest {

    private TxItemsCollapsingTaskItem txItemsCollapsingTaskItem;

    @BeforeEach
    public void setup() {
        this.txItemsCollapsingTaskItem = new TxItemsCollapsingTaskItem();
    }

    @Test
    void shouldNotCollapseItems() {
        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1:0");
        txItem1.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e12")
                .build())
        );
        txItem1.setAmountLcy(BigDecimal.ONE);
        txItem1.setAmountFcy(BigDecimal.TEN);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId("1:1");
        txItem2.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e1212")
                .build())
        );

        txItem2.setAmountLcy(BigDecimal.ONE);
        txItem2.setAmountFcy(BigDecimal.TEN);

        Set<TransactionItemEntity> items = new HashSet<>();
        items.add(txItem1);
        items.add(txItem2);

        val transaction = new TransactionEntity();
        transaction.setId("1");
        transaction.setItems(items);

        txItemsCollapsingTaskItem.run(transaction);

        assertThat(transaction.getItems()).hasSize(2);
        assertThat(transaction.getItems()).extracting(e -> e.getAccountEvent().map(AccountEvent::getCode)).containsExactlyInAnyOrder(Optional.of("e12"), Optional.of("e1212"));
    }

    @Test
    void shouldCollapseItems() {
        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1:0");
        txItem1.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e12")
                .build())
        );

        txItem1.setAmountLcy(BigDecimal.ONE);
        txItem1.setAmountFcy(BigDecimal.TEN);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId("1:1");
        txItem2.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e12")
                .build())
        );

        txItem2.setAmountLcy(BigDecimal.ONE);
        txItem2.setAmountFcy(BigDecimal.TEN);

        Set<TransactionItemEntity> items = new HashSet<>();
        items.add(txItem1);
        items.add(txItem2);

        val transaction = new TransactionEntity();
        transaction.setId("1");
        transaction.setItems(items);

        txItemsCollapsingTaskItem.run(transaction);

        assertThat(transaction.getItems()).hasSize(1);
        assertThat(transaction.getItems()).extracting(TransactionItemEntity::getAmountLcy).containsExactly(BigDecimal.valueOf(2));
        assertThat(transaction.getItems()).extracting(TransactionItemEntity::getAmountFcy).containsExactly(BigDecimal.valueOf(20));
        assertThat(transaction.getItems()).extracting(e -> e.getAccountEvent().map(AccountEvent::getCode)).containsExactly(Optional.of("e12"));
    }

    @Test
    void shouldCollapseSomeItemsAndNotOthers() {
        Set<TransactionEntity> transactions = new HashSet<>();
        TransactionEntity transaction1 = new TransactionEntity();
        transaction1.setId("1");
        Set<TransactionItemEntity> items1 = new HashSet<>();
        TransactionItemEntity txItem1 = new TransactionItemEntity();
        txItem1.setId("1:0");

        txItem1.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e12")
                .build())
        );

        txItem1.setAmountLcy(BigDecimal.ONE);
        txItem1.setAmountFcy(BigDecimal.TEN);
        items1.add(txItem1);
        transaction1.setItems(items1);

        TransactionEntity transaction2 = new TransactionEntity();
        transaction2.setId("2");
        Set<TransactionItemEntity> items2 = new HashSet<>();
        TransactionItemEntity txItem2 = new TransactionItemEntity();
        txItem2.setId("2:0");

        txItem2.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e34")
                .build())
        );

        txItem2.setAmountLcy(BigDecimal.ONE);
        txItem2.setAmountFcy(BigDecimal.TEN);
        items2.add(txItem2);
        transaction2.setItems(items2);

        transactions.add(transaction1);
        transactions.add(transaction2);

        transactions.forEach(txItemsCollapsingTaskItem::run);

        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(TransactionEntity::getId).containsExactlyInAnyOrder("1", "2");
        assertThat(transactions.stream().filter(tx -> tx.getId().equals("1")).findFirst().orElseThrow().getItems()).hasSize(1);
        assertThat(transactions.stream().filter(tx -> tx.getId().equals("2")).findFirst().orElseThrow().getItems()).hasSize(1);
    }

    @Test
    void mustNotCollapseTxItemsForFailedTransactions() {
        val transaction = new TransactionEntity();
        transaction.setId("1");
        transaction.setAutomatedValidationStatus(FAILED);
        val items = new HashSet<TransactionItemEntity>();
        TransactionItemEntity txItem1 = new TransactionItemEntity();
        txItem1.setId("1:0");

        txItem1.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e12")
                .build())
        );

        txItem1.setAmountLcy(BigDecimal.ONE);
        txItem1.setAmountFcy(BigDecimal.TEN);
        items.add(txItem1);
        TransactionItemEntity txItem2 = new TransactionItemEntity();
        txItem2.setId("1:1");

        txItem2.setAccountEvent(Optional.of(AccountEvent.builder()
                .code("e12")
                .build())
        );

        txItem2.setAmountLcy(BigDecimal.ONE);
        txItem2.setAmountFcy(BigDecimal.TEN);
        items.add(txItem2);
        transaction.setItems(items);

        txItemsCollapsingTaskItem.run(transaction);

        assertThat(transaction.getAutomatedValidationStatus()).isEqualTo(FAILED);
        assertThat(transaction.getItems()).hasSize(2);
    }

}
