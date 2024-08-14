package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service.AccountingCorePresentationViewService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ExtractionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.BatchView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.BatchsDetailView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.CORE_CURRENCY_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingCorePresentationConverterTest {

    @Mock
    private TransactionRepositoryGateway transactionRepositoryGateway;

    @Mock
    private AccountingCoreService accountingCoreService;

    @Mock
    private TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;

    @InjectMocks
    private AccountingCorePresentationViewService accountingCorePresentationConverter;

    @Test
    void testAllTransactions() {

        SearchRequest searchRequest = new SearchRequest();
        TransactionItemEntity transactionItem = new TransactionItemEntity();
        TransactionEntity transactionEntity = new TransactionEntity();
        TransactionEntity transactionEntity2 = new TransactionEntity();
        Violation violation = new Violation();
        Account accountDebit = Account.builder().name("debit").code("debit-code").refCode("dcod").build();
        Account accountCredit = new Account().toBuilder().name("credit").code("credit-code").refCode("ccod").build();

        searchRequest.setOrganisationId("org-id");
        searchRequest.setStatus(List.of(ValidationStatus.VALIDATED));
        searchRequest.setTransactionType(List.of(TransactionType.CardCharge));

        transactionEntity.setId("tx-id");
        transactionEntity.setTransactionType(TransactionType.CardCharge);
        transactionEntity.setAutomatedValidationStatus(ValidationStatus.VALIDATED);
        transactionEntity.setTransactionApproved(Boolean.TRUE);
        transactionEntity.setLedgerDispatchApproved(Boolean.FALSE);
        transactionEntity.setStatus(TransactionStatus.FAIL);

        transactionItem.setId("tx-item-id");

        violation.setTxItemId(Optional.of(transactionItem.getId().toString()));
        violation.setSource(Source.ERP);
        violation.setSeverity(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.WARN);
        violation.setCode(CORE_CURRENCY_NOT_FOUND);

        transactionEntity.setItems(Set.of(transactionItem));
        transactionEntity.setViolations(Set.of(violation));

        transactionItem.setAccountDebit(Optional.of(accountDebit));
        transactionItem.setAccountCredit(Optional.of(accountCredit));
        transactionItem.setTransaction(transactionEntity);
        transactionItem.setAmountFcy(BigDecimal.valueOf(1000));
        transactionItem.setAmountLcy(BigDecimal.valueOf(1000));

        transactionEntity2.setId("tx-id2");
        transactionEntity2.setTransactionInternalNumber("tx-id2-internal");
        LocalDate localDate = LocalDate.now();
        transactionEntity2.setEntryDate(localDate);
        transactionEntity2.setTransactionType(TransactionType.CardCharge);
        transactionEntity2.setAutomatedValidationStatus(ValidationStatus.FAILED);
        transactionEntity2.setTransactionApproved(Boolean.FALSE);
        transactionEntity2.setLedgerDispatchApproved(Boolean.TRUE);
        transactionEntity2.setStatus(TransactionStatus.OK);

        when(transactionRepositoryGateway.findAllByStatus(any(), any(), any())).thenReturn(List.of(transactionEntity, transactionEntity2));

        List<TransactionView> result = accountingCorePresentationConverter.allTransactions(searchRequest);

        assertEquals(2, result.size());
        assertEquals("tx-id", result.get(0).getId());
        assertEquals(TransactionType.CardCharge, result.get(0).getTransactionType());
        assertEquals(Boolean.TRUE, result.get(0).isTransactionApproved());
        assertEquals(Boolean.FALSE, result.get(0).isLedgerDispatchApproved());
        assertEquals(TransactionStatus.FAIL, result.get(0).getStatus());
        assertEquals(Boolean.FALSE, result.get(1).isTransactionApproved());
        assertEquals(Boolean.TRUE, result.get(1).isLedgerDispatchApproved());
        assertEquals(ValidationStatus.FAILED, result.get(1).getValidationStatus());
        assertEquals("tx-id2-internal", result.get(1).getInternalTransactionNumber());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        assertEquals(localDate.format(formatter).toString(), result.get(1).getEntryDate().toString());
        assertEquals("tx-item-id", result.get(0).getItems().stream().findFirst().get().getId());

        assertEquals(BigDecimal.valueOf(1000), result.get(0).getItems().stream().findFirst().get().getAmountFcy());
        assertEquals(BigDecimal.valueOf(1000), result.get(0).getItems().stream().findFirst().get().getAmountLcy());
        assertEquals("debit-code", result.get(0).getItems().stream().findFirst().get().getAccountDebitCode());
        assertEquals("credit-code", result.get(0).getItems().stream().findFirst().get().getAccountCreditCode());

        assertEquals(Optional.of("tx-item-id"), result.get(0).getViolations().stream().findFirst().get().getTransactionItemId());

        assertEquals(Source.ERP, result.get(0).getViolations().stream().findFirst().get().getSource());
        assertEquals(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.WARN, result.get(0).getViolations().stream().findFirst().get().getSeverity());
        assertEquals(CORE_CURRENCY_NOT_FOUND, result.get(0).getViolations().stream().findFirst().get().getCode());
    }

    @Test
    void testTransactionDetailSpecific() {

        String transactionId = "tx-id";
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionId);

        when(transactionRepositoryGateway.findById(transactionId)).thenReturn(Optional.of(transactionEntity));

        Optional<TransactionView> result = accountingCorePresentationConverter.transactionDetailSpecific(transactionId);

        assertEquals(true, result.isPresent());
        assertEquals(transactionId, result.get().getId());
    }

    @Test
    void testBatchDetail() {

        TransactionItemEntity transactionItem = new TransactionItemEntity();
        transactionItem.setId("txItemId");
        transactionItem.setAmountLcy(BigDecimal.valueOf(100));
        Violation violation = new Violation();
        violation.setTxItemId(Optional.of("txItemId"));
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now();
        FilteringParameters filteringParameters = new FilteringParameters("pros", List.of(TransactionType.CardCharge), from, to, YearMonth.now(), YearMonth.now(), Collections.singletonList("somestring"));

        String batchId = "batch-id";
        TransactionBatchEntity transactionBatchEntity = new TransactionBatchEntity();
        BatchStatistics batchStatistics = BatchStatistics.builder().totalTransactionsCount(10).failedTransactionsCount(1).approvedTransactionsCount(9).finalizedTransactionsCount(6).processedTransactionsCount(8).build();

        TransactionEntity transaction1 = new TransactionEntity();
        transaction1.setId("tx-id1");
        TransactionEntity transaction2 = new TransactionEntity();
        transaction2.setId("tx-id2");

        transaction1.setItems(Set.of(transactionItem));
        transaction1.setViolations(Set.of(violation));
        transactionItem.setTransaction(transaction1);

        transactionBatchEntity.setBatchStatistics(batchStatistics);
        transactionBatchEntity.setId(batchId);
        transactionBatchEntity.setCreatedAt(LocalDateTime.now());
        transactionBatchEntity.setUpdatedAt(LocalDateTime.now());
        transactionBatchEntity.setFilteringParameters(filteringParameters);
        Set<TransactionEntity> transactions = new LinkedHashSet<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactionBatchEntity.setTransactions(transactions);
        transactionBatchEntity.setStatus(TransactionBatchStatus.CREATED);

        transaction1.setBatchId(batchId);
        transaction2.setBatchId(batchId);

        when(transactionBatchRepositoryGateway.findById(batchId)).thenReturn(Optional.of(transactionBatchEntity));

        Optional<BatchView> result = accountingCorePresentationConverter.batchDetail(batchId);

        assertEquals(true, result.isPresent());
        assertEquals(batchId, result.get().getId());
        assertEquals(10, result.get().getBatchStatistics().getTotal());
        assertEquals(1, result.get().getBatchStatistics().getInvalid());
        assertEquals(0, result.get().getBatchStatistics().getPublish());
        assertEquals(2, result.get().getBatchStatistics().getPending());
        assertEquals(0, result.get().getBatchStatistics().getPublished());
        assertEquals(9, result.get().getBatchStatistics().getApprove());
        assertEquals(2, result.get().getTransactions().stream().count());

        TransactionView resultTx1 = result.get().getTransactions().stream().filter(
                transactionView -> transactionView.getId() == "tx-id1"
        ).findFirst().get();
        assertEquals("tx-id1", resultTx1.getId());
        YearMonth today = YearMonth.now();

        assertEquals(today, result.get().getFilteringParameters().getAccountingPeriodFrom());
        assertEquals(today, result.get().getFilteringParameters().getAccountingPeriodTo());
        assertEquals(from, result.get().getFilteringParameters().getFrom());
        assertEquals(to, result.get().getFilteringParameters().getTo());
        assertEquals(List.of(TransactionType.CardCharge), result.get().getFilteringParameters().getTransactionTypes());
        assertEquals(List.of("somestring"), result.get().getFilteringParameters().getTransactionNumbers());
        assertEquals("txItemId", resultTx1.getItems().stream().findFirst().get().getId());
        assertEquals("txItemId", resultTx1.getViolations().stream().findFirst().get().getTransactionItemId().get());
    }

    @Test
    void testListAllBatchModel() {

        BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
        batchSearchRequest.setOrganisationId("org-id");
        batchSearchRequest.setFrom(LocalDate.now());
        batchSearchRequest.setTransactionTypes(Set.of(TransactionType.CardCharge));
        FilteringParameters filteringParameters = new FilteringParameters("pros", List.of(TransactionType.CardCharge), LocalDate.now(), LocalDate.now(), YearMonth.now(), YearMonth.now(), Collections.singletonList("batch"));
        TransactionBatchEntity transactionBatchEntity = new TransactionBatchEntity();
        BatchStatistics batchStatistics = BatchStatistics.builder().totalTransactionsCount(10).failedTransactionsCount(1).approvedTransactionsCount(9).finalizedTransactionsCount(10).build();
        transactionBatchEntity.setId("batch-id");
        transactionBatchEntity.setCreatedAt(LocalDateTime.now());
        transactionBatchEntity.setUpdatedAt(LocalDateTime.now());
        transactionBatchEntity.setFilteringParameters(filteringParameters);
        transactionBatchEntity.setBatchStatistics(batchStatistics);

        when(transactionBatchRepositoryGateway.findByFilter(batchSearchRequest)).thenReturn(List.of(transactionBatchEntity));
        when(transactionBatchRepositoryGateway.findByFilterCount(batchSearchRequest)).thenReturn(Long.valueOf(1));

        BatchsDetailView batchsDetailView = accountingCorePresentationConverter.listAllBatch(batchSearchRequest);
        List<BatchView> result = batchsDetailView.getBatchs();

        assertEquals(1, result.size());
        assertEquals(1, batchsDetailView.getTotal());
        assertEquals(1, batchsDetailView.getBatchs().stream().count());
        assertEquals("batch-id", result.iterator().next().getId());
        assertEquals(TransactionBatchStatus.CREATED, result.iterator().next().getStatus());

    }

    @Test
    void testExtractionTrigger() {

        ExtractionRequest extractionRequest = new ExtractionRequest();
        extractionRequest.setDateFrom("2022-01-01");
        extractionRequest.setDateTo("2022-12-31");
        extractionRequest.setOrganisationId("org-id");
        extractionRequest.setTransactionType(List.of(TransactionType.CardCharge));
        extractionRequest.setTransactionNumbers(List.of("num1", "num2"));

        accountingCorePresentationConverter.extractionTrigger(extractionRequest);
        Mockito.verify(accountingCoreService, Mockito.times(1)).scheduleIngestion(Mockito.any(UserExtractionParameters.class));

    }
}
