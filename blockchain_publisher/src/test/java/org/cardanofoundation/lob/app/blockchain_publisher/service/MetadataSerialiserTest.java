package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataMap;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataList;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataSerialiserTest {

    private Clock fixedClock;

    private MetadataSerialiser metadataSerialiser;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2023-06-01T10:15:30.00Z"), ZoneId.of("UTC"));
        metadataSerialiser = new MetadataSerialiser(fixedClock);
    }

    @Test
    void testSerialiseToMetadataMap() {
        // Given
        val accountingPeriod = YearMonth.of(2023, 2);

        String organisationId = "org123";
        Organisation organisation = new Organisation();
        organisation.setId(organisationId);
        organisation.setName("Test Organisation");
        organisation.setTaxIdNumber("123456789");
        organisation.setCurrencyId("USD");
        organisation.setCountryCode("US");

        TransactionEntity transaction = new TransactionEntity();
        transaction.setId("tx123");
        transaction.setInternalNumber("1");
        transaction.setBatchId("batch1");
        transaction.setTransactionType(TransactionType.FxRevaluation);
        transaction.setEntryDate(LocalDate.now(fixedClock));
        transaction.setAccountingPeriod(accountingPeriod);
        transaction.setOrganisation(organisation);

        TransactionItemEntity item1 = new TransactionItemEntity();
        item1.setId("item1");
        item1.setAmountFcy(new BigDecimal("100.00"));
        item1.setFxRate(new BigDecimal("1.0"));
        item1.setDocument(Document.builder()
                .num("doc1")
                .currency(Currency.builder()
                        .customerCode("USD")
                        .build()).build()
        );

        TransactionItemEntity item2 = new TransactionItemEntity();
        item2.setId("item2");
        item2.setAmountFcy(new BigDecimal("200.00"));
        item2.setFxRate(new BigDecimal("1.0"));
        item2.setDocument(Document.builder()
                .num("doc2")
                .currency(Currency.builder()
                        .customerCode("USD")
                        .build()).build()
        );

        TransactionItemEntity item3 = new TransactionItemEntity();
        item3.setId("item3");
        item3.setAmountFcy(new BigDecimal("300.00"));
        item3.setFxRate(new BigDecimal("1.0"));
        item3.setDocument(Document.builder()
                .num("doc3")
                .currency(Currency.builder()
                        .customerCode("USD")
                        .build())
                .build()
        );

        transaction.setItems(Set.of(item1, item2, item3));

        Set<TransactionEntity> transactions = Set.of(transaction);
        long creationSlot = 12345;

        // When
        MetadataMap result = metadataSerialiser.serialiseToMetadataMap(organisationId, transactions, creationSlot);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("metadata")).isInstanceOf(MetadataMap.class);
        MetadataMap metadata = (MetadataMap) result.get("metadata");

        assertThat(metadata.get("creation_slot")).isEqualTo(BigInteger.valueOf(creationSlot));
        assertThat(metadata.get("timestamp")).isEqualTo("2023-06-01T10:15:30Z");
        assertThat(metadata.get("version")).isEqualTo(MetadataSerialiser.VERSION);

        assertThat(result.get("org")).isInstanceOf(MetadataMap.class);
        MetadataMap orgMap = (MetadataMap) result.get("org");

        assertThat(orgMap.get("id")).isEqualTo(organisation.getId());
        assertThat(orgMap.get("name")).isEqualTo(organisation.getName());
        assertThat(orgMap.get("tax_id_number")).isEqualTo(organisation.getTaxIdNumber());
        assertThat(orgMap.get("currency_id")).isEqualTo(organisation.getCurrencyId());
        assertThat(orgMap.get("country_code")).isEqualTo(organisation.getCountryCode());

        assertThat(result.get("txs")).isInstanceOf(CBORMetadataList.class);
        CBORMetadataList txsList = (CBORMetadataList) result.get("txs");

        assertThat(txsList.size() == 1).isEqualTo(true);
        MetadataMap txMap = (MetadataMap) txsList.getValueAt(0);

        assertThat(txMap.get("id")).isEqualTo(transaction.getId());
        assertThat(txMap.get("number")).isEqualTo(transaction.getInternalNumber());
        assertThat(txMap.get("batch_id")).isEqualTo(transaction.getBatchId());
        assertThat(txMap.get("type")).isEqualTo(transaction.getTransactionType().name());
        assertThat(txMap.get("date")).isEqualTo(transaction.getEntryDate().toString());
        assertThat(txMap.get("accounting_period")).isEqualTo(accountingPeriod.toString());

        assertThat(txMap.get("items")).isInstanceOf(CBORMetadataList.class);
        val itemsList = (CBORMetadataList) txMap.get("items");

        assertThat(itemsList.size() == 3).isEqualTo(true);

        for (int i = 0; i < itemsList.size(); i++) {
            MetadataMap itemMap = (MetadataMap) itemsList.getValueAt(i);
            TransactionItemEntity item = transaction.getItems().stream()
                    .filter(it -> it.getId().equals(itemMap.get("id")))
                    .findFirst().orElseThrow();

            assertThat(itemMap.get("id")).isEqualTo(item.getId());
            assertThat(itemMap.get("amount")).isEqualTo(item.getAmountFcy().toEngineeringString());
            assertThat(itemMap.get("fx_rate")).isEqualTo(item.getFxRate().toEngineeringString());
            assertThat(itemMap.get("document")).isInstanceOf(MetadataMap.class);
        }

        val itemMap1 = assertContainsItem("item1", itemsList);
        val itemMap2 = assertContainsItem("item2", itemsList);
        val itemMap3 = assertContainsItem("item3", itemsList);

        assertThat(itemMap1.get("id")).isEqualTo("item1");
        assertThat(itemMap1.get("amount")).isEqualTo(item1.getAmountFcy().toEngineeringString());
        assertThat(itemMap1.get("fx_rate")).isEqualTo(item1.getFxRate().toEngineeringString());
        assertThat(itemMap1.get("document")).isInstanceOf(MetadataMap.class);

        assertThat(itemMap2.get("id")).isEqualTo(item2.getId());
        assertThat(itemMap2.get("amount")).isEqualTo(item2.getAmountFcy().toEngineeringString());
        assertThat(itemMap2.get("fx_rate")).isEqualTo(item2.getFxRate().toEngineeringString());
        assertThat(itemMap2.get("document")).isInstanceOf(MetadataMap.class);

        assertThat(itemMap3.get("id")).isEqualTo(item3.getId());
        assertThat(itemMap3.get("amount")).isEqualTo(item3.getAmountFcy().toEngineeringString());
        assertThat(itemMap3.get("fx_rate")).isEqualTo(item3.getFxRate().toEngineeringString());
        assertThat(itemMap3.get("document")).isInstanceOf(MetadataMap.class);
    }

    private MetadataMap assertContainsItem(String id, CBORMetadataList items) {
        for (int i = 0; i < items.size(); i++) {
            MetadataMap itemMap = (MetadataMap) items.getValueAt(i);

            if (itemMap.get("id").equals(id)) {
                return itemMap;
            }
        }

        throw new AssertionError(STR."Item with id \{id} not found");
    }

}
