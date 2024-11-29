package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.CREDIT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.DEBIT;

class TransactionItemEntityTest {

    @Test
    void checkOperationTypeWorksProperly() {
        val item1 = new TransactionItemEntity();
        item1.setId("1");
        item1.setAmountLcy(BigDecimal.valueOf(0.06)); // positive implies debit

        val item2 = new TransactionItemEntity();
        item2.setId("2");
        item2.setAmountLcy(BigDecimal.valueOf(-0.99)); // negative implies credit

        val item3 = new TransactionItemEntity();
        item3.setId("3");
        item3.setAmountLcy(BigDecimal.valueOf(0.93)); // positive implies debit

        val item4 = new TransactionItemEntity();
        item4.setId("4");
        item4.setAmountLcy(BigDecimal.valueOf(0));

        assertThat(item1.getOperationType()).isEqualTo(Optional.of(DEBIT));
        assertThat(item2.getOperationType()).isEqualTo(Optional.of(CREDIT));
        assertThat(item3.getOperationType()).isEqualTo(Optional.of(DEBIT));
        assertThat(item4.getOperationType()).isEqualTo(Optional.empty());
    }

}
