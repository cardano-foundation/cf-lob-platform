package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionVersionAlgo.ERP_SOURCE;

@Slf4j
class TransactionVersionCalculatorTest {

    @Test
    public void compute() {
        val org = Organisation.builder()
                .id("401cad588bb2152f5c7ea0646ed84dd7f1b233dc73c3463d721f43e117a0e8ad")
                .build();


        val tx = createTx(org);

        val tHash = TransactionVersionCalculator.compute(ERP_SOURCE, tx);

        assertThat(tHash).isNotNull();

        assertThat(tHash).isEqualTo("5e4efe47f88f1dd624f6b8828de435382936074470771a8cdd5c49e089a5f240");
    }

    private static @NotNull TransactionEntity createTx(Organisation org) {
        val t = new TransactionEntity();
        t.setTransactionType(FxRevaluation);
        t.setEntryDate(LocalDate.of(2021, 1, 1));
        t.setOrganisation(org);
        t.setTransactionInternalNumber("FxRevaluation-1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1");
        txItem1.setAmountFcy(BigDecimal.valueOf(100.10));
        txItem1.setAmountLcy(BigDecimal.valueOf(100.20));

        txItem1.setAccountDebit(Account.builder()
                .code("1000")
                .name("Cash")
                .refCode("r1000")
                .build()
        );

        txItem1.setAccountCredit(Account.builder()
                .code("2000")
                .name("Bank")
                .refCode("r2000")
                .build()
        );

        txItem1.setDocument(Document.builder()
                        .num("doc-1")
                        .vat(Vat.builder()
                                .customerCode("C100")
                                .build())
                        .currency(Currency.builder()
                                .id("ISO_4217:CHF")
                                .customerCode("CHF")
                                .build())
                        .counterparty(Counterparty.builder()
                                .customerCode("C100")
                                .build())
                .build());

        val txItem2 = new TransactionItemEntity();
        txItem2.setId("2");
        txItem2.setAmountFcy(BigDecimal.valueOf(100.10));
        txItem2.setAmountLcy(BigDecimal.valueOf(100.20));

        txItem2.setAccountDebit(Account.builder()
                .code("2000")
                .name("Cash")
                .refCode("21000")
                .build()
        );

        txItem2.setAccountCredit(Account.builder()
                .code("4000")
                .name("Bank")
                .refCode("r3000")
                .build()
        );

        txItem2.setDocument(Document.builder()
                .num("doc-2")
                .vat(Vat.builder()
                        .customerCode("C200")
                        .build())
                .currency(Currency.builder()
                        .id("ISO_4217:CHF")
                        .customerCode("CHF")
                        .build())
                .counterparty(Counterparty.builder()
                        .customerCode("C200")
                        .build())
                .build());

        t.setItems(Set.of(txItem1, txItem2));

        return t;
    }

}
