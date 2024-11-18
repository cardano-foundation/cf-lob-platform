package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.mockito.Mockito.*;

class JournalAccountAmountsEnrichmentTaskItemTest {

    private JournalAccountAmountsEnrichmentTaskItem taskItem;

    @BeforeEach
    void setUp() {
        taskItem = new JournalAccountAmountsEnrichmentTaskItem();
    }

    @Test
    void shouldNotProcessNonJournalTransaction() {
        // Given
        TransactionEntity transaction = mock(TransactionEntity.class);
        when(transaction.getTransactionType()).thenReturn(TransactionType.FxRevaluation); // A type other than Journal

        // When
        taskItem.run(transaction);

        // Then
        verify(transaction, times(1)).getTransactionType();
        verifyNoMoreInteractions(transaction);
    }

    @Test
    void shouldProcessJournalTransaction() {
        // Given
        TransactionItemEntity item1 = new TransactionItemEntity();
        item1.setId("1");
        item1.setAmountFcy(new BigDecimal("-100.00"));
        item1.setAmountLcy(new BigDecimal("-200.00"));

        TransactionItemEntity item2 = new TransactionItemEntity();
        item2.setId("2");
        item2.setAmountFcy(new BigDecimal("300.00"));
        item2.setAmountLcy(new BigDecimal("400.00"));

        TransactionEntity transaction = mock(TransactionEntity.class);
        when(transaction.getTransactionType()).thenReturn(Journal);
        when(transaction.getItems()).thenReturn(Set.of(item1, item2));

        // When
        taskItem.run(transaction);

        // Then
        assertThat(item1.getAmountFcy()).isEqualByComparingTo("-100.00");
        assertThat(item1.getAmountLcy()).isEqualByComparingTo("-200.00");
        assertThat(item2.getAmountFcy()).isEqualByComparingTo("300.00");
        assertThat(item2.getAmountLcy()).isEqualByComparingTo("400.00");

        verify(transaction, times(1)).getTransactionType();
        verify(transaction, times(1)).getItems();
        verifyNoMoreInteractions(transaction);
    }

}
