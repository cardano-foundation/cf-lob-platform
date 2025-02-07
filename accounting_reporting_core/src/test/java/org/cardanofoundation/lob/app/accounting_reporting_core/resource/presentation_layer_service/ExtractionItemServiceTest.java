package org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.val;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemExtractionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ExtractionTransactionView;

@ExtendWith(MockitoExtension.class)
class ExtractionItemServiceTest {

    @Mock
    private TransactionItemExtractionRepository transactionItemExtractionRepository;

    @Test
    void findTransactionItemsTest() {
        val document = Document.builder()
                .currency(Currency.builder()
                        .customerCode("EUR")
                        .build())
                .build();
        val tx = new TransactionEntity();
        tx.setId("TxId1");
        tx.setTransactionInternalNumber("1");
        tx.setOrganisation(Organisation.builder().id("orgId1").build());
        tx.setTransactionType(TransactionType.FxRevaluation);


        val item1 = new TransactionItemEntity();
        item1.setId("item1");
        item1.setDocument(Optional.of(document));
        item1.setAmountFcy(BigDecimal.valueOf(1));
        item1.setAmountLcy(BigDecimal.valueOf(1));
        tx.setItems(Set.of(item1));

        item1.setTransaction(tx);

        Mockito.when(transactionItemExtractionRepository.findByItemAccount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(List.of(item1));
        ExtractionItemService extractionItemService = new ExtractionItemService(transactionItemExtractionRepository);

        ExtractionTransactionView result = extractionItemService.findTransactionItems(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        assertInstanceOf(ExtractionTransactionView.class, result);
        assertEquals(1L, result.getTotal());
        verifyNoMoreInteractions(transactionItemExtractionRepository);

    }
}
