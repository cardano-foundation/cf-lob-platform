package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JournalAccountCreditEnrichmentTaskItemTest {

    private static final String DUMMY_ACCOUNT = "0000000000";

    @Mock
    private OrganisationPublicApiIF organisationPublicApiIF;
    @Mock
    private Organisation organisation;

    @InjectMocks
    private JournalAccountCreditEnrichmentTaskItem taskItem;

    private TransactionEntity transaction;

    @Test
    void should_Not_Run_Because_It_Is_Not_A_Journal_Transaction() {
        val organisationId = "org1";

        val items = new LinkedHashSet<TransactionItemEntity>();
        transaction = new TransactionEntity();
        transaction.setOrganisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation.builder()
                .id(organisationId)
                .build()
        );
        transaction.setTransactionType(FxRevaluation);

        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1");
        txItem1.clearAccountCodeCredit();
        items.add(txItem1);

        transaction.setItems(items);

        taskItem.run(transaction);

        // check that account credit was not manipulated
        assertThat(txItem1.getAccountCredit()).isEmpty();
    }

    @Test
    void should_Not_Run_Because_Dummy_Account_Is_Missing() {
        val organisationId = "org1";

        val items = new LinkedHashSet<TransactionItemEntity>();
        transaction = new TransactionEntity();
        transaction.setOrganisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation.builder()
                .id(organisationId)
                .build()
        );
        transaction.setTransactionType(Journal);

        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1");
        txItem1.clearAccountCodeCredit();
        items.add(txItem1);

        transaction.setItems(items);

        when(organisationPublicApiIF.findByOrganisationId(eq(organisationId))).thenReturn(Optional.of(organisation));
        when(organisation.getDummyAccount()).thenReturn(Optional.empty());

        taskItem.run(transaction);

        // check that account credit was not manipulated
        assertThat(txItem1.getAccountCredit()).isEmpty();
    }

    @Test
    void should_Not_Run_Because_Not_All_Credit_Accounts_Are_Missing() {
        val organisationId = "org1";

        val items = new LinkedHashSet<TransactionItemEntity>();
        transaction = new TransactionEntity();
        transaction.setOrganisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation.builder()
                .id(organisationId)
                .build()
        );
        transaction.setTransactionType(Journal);

        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1");
        txItem1.clearAccountCodeCredit();
        items.add(txItem1);

        val txItem2 = new TransactionItemEntity();
        txItem2.setId("2");
        txItem2.setAccountCredit(Optional.of(Account.builder()
                .code("1234567890")
                .build())
        );
        items.add(txItem2);

        transaction.setItems(items);

        when(organisationPublicApiIF.findByOrganisationId(eq(organisationId))).thenReturn(Optional.of(organisation));
        when(organisation.getDummyAccount()).thenReturn(Optional.empty());

        taskItem.run(transaction);

        // check that account credit was not manipulated
        assertThat(txItem1.getAccountCredit()).isEmpty();
        assertThat(txItem2.getAccountCredit()).isPresent();
    }

    @Test
    void should_Set_Credit_From_Debit_If_Conditions_Met() {

        val items = new LinkedHashSet<TransactionItemEntity>();
        transaction = new TransactionEntity();
        transaction.setOrganisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation.builder()
                .id("org1")
                .build()
        );
        transaction.setTransactionType(Journal);

        val item1 = new TransactionItemEntity();
        item1.setId("1");

        item1.setAccountDebit(Optional.of(Account.builder()
                .code("4102110100")
                .build())
        );

        item1.setAmountLcy(BigDecimal.valueOf(988.86)); // positive implies debit
        items.add(item1);

        val item2 = new TransactionItemEntity();
        item2.setId("2");
        item2.setAccountDebit(Optional.of(Account.builder()
                .code("4102120100")
                .build()
        )
        );

        item2.setAmountLcy(BigDecimal.valueOf(-188.50)); // negative implies credit
        items.add(item2);

        val item3 = new TransactionItemEntity();
        item3.setId("3");
        item3.setAccountDebit(Optional.of(Account.builder()
                .code("4102140100")
                .build())
        );

        item3.setAmountLcy(BigDecimal.valueOf(148.64));
        items.add(item3);

        val item4 = new TransactionItemEntity();
        item4.setId("4");

        item4.setAccountDebit(Optional.of(Account.builder()
                .code("5205140100")
                .build())
        );

        item4.setAmountLcy(BigDecimal.valueOf(-949.00));
        items.add(item4);

        val item5 = new TransactionItemEntity();
        item5.setId("5");

        item5.setAccountDebit(Optional.of(Account.builder()
                .code("5208110100")
                .build())
        );

        item5.setAmountLcy(BigDecimal.valueOf(-528.5));
        items.add(item5);

        val item6 = new TransactionItemEntity();
        item6.setId("6");

        item6.setAccountDebit(Optional.of(Account.builder()
                .code("5208120100")
                .build())

        );
        item6.setAmountLcy(BigDecimal.valueOf(-147.30));
        items.add(item6);

        val item7 = new TransactionItemEntity();
        item7.setId("7");

        item7.setAccountDebit(Optional.of(Account.builder()
                .code("5205140100")
                .build())
        );

        item7.setAmountLcy(BigDecimal.valueOf(675.80));
        items.add(item7);

        val item8 = new TransactionItemEntity();
        item8.setId("8");

        item8.setAccountDebit(Optional.of(Account.builder()
                .code("1203210100")
                .build()
        ));

        item8.setAmountLcy(BigDecimal.valueOf(-925.40));
        items.add(item8);

        val item9 = new TransactionItemEntity();
        item9.setId("9");

        item9.setAccountDebit(Optional.of(Account.builder()
                .code("5205140100")
                .build()
        ));

        item9.setAmountLcy(BigDecimal.valueOf(925.40));
        items.add(item9);

        val item10 = new TransactionItemEntity();
        item10.setId("10");

        item10.setAccountDebit(Optional.of(Account.builder()
                .code("5205140101")
                .build())
        );

        item10.setAmountLcy(BigDecimal.valueOf(0));
        items.add(item10);

        transaction.setItems(items);

        when(organisationPublicApiIF.findByOrganisationId(eq("org1"))).thenReturn(Optional.of(organisation));
        when(organisation.getDummyAccount()).thenReturn(Optional.of(DUMMY_ACCOUNT));

        taskItem.run(transaction);

        assertThat(item1.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("4102110100");
        assertThat(item2.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item3.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("4102140100");
        assertThat(item4.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item5.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item6.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item7.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("5205140100");
        assertThat(item8.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item9.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("5205140100");
        assertThat(item10.getAccountDebit().map(Account::getCode).orElseThrow()).isEqualTo("5205140101");

        assertThat(item1.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item2.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("4102120100");
        assertThat(item3.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item4.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("5205140100");
        assertThat(item5.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("5208110100");
        assertThat(item6.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("5208120100");
        assertThat(item7.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item8.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("1203210100");
        assertThat(item9.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
        assertThat(item10.getAccountCredit().map(Account::getCode).orElseThrow()).isEqualTo("0000000000");
    }

}
