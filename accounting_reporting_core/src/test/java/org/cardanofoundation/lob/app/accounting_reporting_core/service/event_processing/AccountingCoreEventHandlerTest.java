package org.cardanofoundation.lob.app.accounting_reporting_core.service.event_processing;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TxsLedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreTransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreEventHandler;
import org.cardanofoundation.lob.app.accounting_reporting_core.test_configs.JaversConfig;
import org.cardanofoundation.lob.app.accounting_reporting_core.test_configs.JpaConfig;
import org.cardanofoundation.lob.app.accounting_reporting_core.test_configs.ModulithEventConfig;
import org.cardanofoundation.lob.app.accounting_reporting_core.test_configs.TimeConfig;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;

@SpringBootTest(classes = {JaversConfig.class, TimeConfig.class, JpaConfig.class, ModulithEventConfig.class})
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
@EnableAutoConfiguration
@ComponentScan(basePackages = {"org.cardanofoundation.lob.app.accounting_reporting_core","org.cardanofoundation.lob.app.organisation","org.cardanofoundation.lob.app.blockchain_reader"})
class AccountingCoreEventHandlerTest {

    @Autowired
    private AccountingCoreEventHandler accountingCoreEventHandler;
    @Autowired
    private AccountingCoreTransactionRepository accountingCoreTransactionRepository;
    @Autowired
    private TransactionBatchRepository transactionBatchRepository;

    @BeforeAll
    public static void clearDatabase(@Autowired Flyway flyway){
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void testHandleLedgerUpdatedEvent() {
        TransactionBatchEntity transactionBatchEntity = new TransactionBatchEntity();
        transactionBatchEntity.setId("batchId");
        transactionBatchEntity.setFilteringParameters(FilteringParameters.builder()
                        .organisationId("testOrg")
                        .from(LocalDate.now().minusWeeks(1))
                        .to(LocalDate.now().plusWeeks(1))
                        .accountingPeriodFrom(LocalDate.now().minusYears(1))
                        .accountingPeriodTo(LocalDate.now().plusYears(1))
                .build());
        transactionBatchRepository.save(transactionBatchEntity);

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId("txId");
        transactionEntity.setOrganisation(Organisation.builder().id("testOrg").build());
        transactionEntity.setTransactionInternalNumber("txInternalNumber");
        transactionEntity.setBatchId("batchId");
        transactionEntity.setAccountingPeriod(YearMonth.now());
        transactionEntity.setEntryDate(LocalDate.now());
        transactionEntity.setTransactionType(TransactionType.BillCredit);
        transactionEntity.setLedgerDispatchStatus(LedgerDispatchStatus.NOT_DISPATCHED);
        accountingCoreTransactionRepository.saveAndFlush(transactionEntity);

        TxsLedgerUpdatedEvent build = TxsLedgerUpdatedEvent.builder()
                .metadata(EventMetadata.create("1.0", "testUser"))
                .organisationId("testOrg")
                .statusUpdates(Set.of(new TxStatusUpdate(transactionEntity.getId(), LedgerDispatchStatus.MARK_DISPATCH, Set.of())))
                .build();
        // sending events twice to check if the status is updated correctly and no exceptions are thrown
        accountingCoreEventHandler.handleLedgerUpdatedEvent(build);
        accountingCoreEventHandler.handleLedgerUpdatedEvent(build);

        Optional<TransactionEntity> txEntity = accountingCoreTransactionRepository.findById("txId");
        Assertions.assertTrue(txEntity.isPresent());
        Assertions.assertEquals(LedgerDispatchStatus.MARK_DISPATCH, txEntity.get().getLedgerDispatchStatus());
    }
}
