package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ExtractionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.AMOUNT_FCY_IS_ZERO;

@Service
@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
// presentation layer service
public class AccountingCorePresentationViewService {
    private final TransactionRepositoryGateway transactionRepositoryGateway;
    private final AccountingCoreService accountingCoreService;
    private final TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;

    public List<TransactionView> allTransactions(SearchRequest body) {
        List<TransactionEntity> transactions = transactionRepositoryGateway.findAllByStatus(body.getOrganisationId(), body.getStatus(), body.getTransactionType());

        return
                transactions.stream().map(this::getTransactionView).toList()
                ;
    }

    public Optional<TransactionView> transactionDetailSpecific(String transactionId) {

        Optional<TransactionEntity> transactionEntity = transactionRepositoryGateway.findById(transactionId);
        return transactionEntity.map(this::getTransactionView);
    }

    public Optional<BatchView> batchDetail(String batchId) {
        return transactionBatchRepositoryGateway.findById(batchId).map(transactionBatchEntity -> {

                    val transactions = this.getTransaction(transactionBatchEntity);
                    val statistic = this.getStatisticts(transactionBatchEntity.getBatchStatistics());
                    val filteringParameters = this.getFilteringParameters(transactionBatchEntity.getFilteringParameters());
                    return new BatchView(
                            transactionBatchEntity.getId(),
                            transactionBatchEntity.getCreatedAt().toString(),
                            transactionBatchEntity.getUpdatedAt().toString(),
                            transactionBatchEntity.getCreatedBy(),
                            transactionBatchEntity.getUpdatedBy(),
                            transactionBatchEntity.getOrganisationId(),
                            transactionBatchEntity.getStatus(),
                            statistic,
                            filteringParameters,
                            transactions

                    );
                }
        );
    }

    private BatchStatisticsView getStatisticts(Optional<BatchStatistics> batchStatistics) {

        Optional<BatchStatistics> statistics = batchStatistics.stream().findFirst();

        return new BatchStatisticsView(
                statistics.flatMap(BatchStatistics::getProcessedTransactionsCount).orElse(0),
                Math.abs(statistics.flatMap(BatchStatistics::getFailedTransactionsCount).orElse(0) - statistics.flatMap(BatchStatistics::getApprovedTransactionsCount).orElse(0)),
                statistics.flatMap(BatchStatistics::getFailedTransactionsCount).orElse(0),
                statistics.flatMap(BatchStatistics::getApprovedTransactionsCount).orElse(0),
                statistics.flatMap(BatchStatistics::getFinalizedTransactionsCount).orElse(0),
                statistics.flatMap(BatchStatistics::getTotalTransactionsCount).orElse(0)
        );

    }

    private FilteringParametersView getFilteringParameters(FilteringParameters filteringParameters) {
        return new FilteringParametersView(
                filteringParameters.getTransactionTypes(),
                filteringParameters.getFrom(),
                filteringParameters.getTo(),
                filteringParameters.getAccountingPeriodFrom(),
                filteringParameters.getAccountingPeriodTo(),
                filteringParameters.getTransactionNumbers()
        );
    }

    public BatchsDetailView listAllBatch(BatchSearchRequest body) {

        BatchsDetailView batchDetail = new BatchsDetailView();

        List<BatchView> batches = transactionBatchRepositoryGateway.findByFilter(body).stream().map(

                transactionBatchEntity -> new BatchView(
                        transactionBatchEntity.getId(),
                        transactionBatchEntity.getCreatedAt().toString(),
                        transactionBatchEntity.getUpdatedAt().toString(),
                        transactionBatchEntity.getCreatedBy(),
                        transactionBatchEntity.getUpdatedBy(),
                        transactionBatchEntity.getOrganisationId(),
                        transactionBatchEntity.getStatus(),
                        this.getStatisticts(transactionBatchEntity.getBatchStatistics()),
                        this.getFilteringParameters(transactionBatchEntity.getFilteringParameters()),
                        Set.of()
                )
        ).toList();

        batchDetail.setBatchs(batches);
        batchDetail.setTotal(Long.valueOf(transactionBatchRepositoryGateway.findByFilterCount(body)));
        return batchDetail;
    }

    @Transactional
    public void extractionTrigger(ExtractionRequest body) {
        val fp = UserExtractionParameters.builder()
                .from(LocalDate.parse(body.getDateFrom()))
                .to(LocalDate.parse(body.getDateTo()))
                .organisationId(body.getOrganisationId())
                .transactionTypes(body.getTransactionType())
                .transactionNumbers(body.getTransactionNumbers())
                .build();

        accountingCoreService.scheduleIngestion(fp);

    }

    private Set<TransactionView> getTransaction(TransactionBatchEntity transactionBatchEntity) {
        return transactionBatchEntity.getTransactions().stream()
                .map(this::getTransactionView)
                .collect(Collectors.toSet());
    }

    private TransactionView getTransactionView(TransactionEntity transactionEntity) {
        return new TransactionView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                transactionEntity.getAutomatedValidationStatus(),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getAmountLcy(transactionEntity),
                getTransactionItemView(transactionEntity),
                getViolation(transactionEntity),
                transactionEntity.getStatus()
        );
    }

    private Set<TransactionItemView> getTransactionItemView(TransactionEntity transaction) {
        return transaction.getItems().stream().map(item -> {

            return new TransactionItemView(
                    item.getId(),
                    item.getAccountDebit().map(account -> account.getCode()).orElse(""),
                    item.getAccountDebit().flatMap(account -> account.getName()).orElse(""),
                    item.getAccountDebit().flatMap(account -> account.getRefCode()).orElse(""),
                    item.getAccountCredit().map(account -> account.getCode()).orElse(""),
                    item.getAccountCredit().flatMap(account -> account.getName()).orElse(""),
                    item.getAccountCredit().flatMap(account -> account.getRefCode()).orElse(""),
                    item.getAmountFcy(),
                    item.getAmountLcy(),
                    item.getFxRate(),
                    item.getCostCenter().map(CostCenter::getCustomerCode).orElse(""),
                    item.getCostCenter().flatMap(CostCenter::getExternalCustomerCode).orElse(""),
                    item.getCostCenter().flatMap(CostCenter::getName).orElse(""),
                    item.getProject().map(Project::getCustomerCode).orElse(""),
                    item.getProject().flatMap(Project::getName).orElse(""),
                    item.getProject().flatMap(Project::getExternalCustomerCode).orElse(""),
                    item.getAccountEvent().map(AccountEvent::getCode).orElse(""),
                    item.getAccountEvent().map(AccountEvent::getName).orElse(""),
                    item.getDocument().map(Document::getNum).orElse(""),
                    item.getDocument().map(document -> document.getCurrency().getCustomerCode()).orElse(""),
                    item.getDocument().flatMap(document -> document.getVat().map(Vat::getCustomerCode)).orElse(""),
                    item.getDocument().flatMap(document -> document.getVat().flatMap(Vat::getRate)).orElse(BigDecimal.ZERO),
                    item.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getCustomerCode)).orElse(""),
                    item.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getType)).orElse(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR),
                    item.getDocument().flatMap(document -> document.getCounterparty().flatMap(Counterparty::getName)).orElse("")


            );
        }).collect(Collectors.toSet());
    }

    private Set<ViolationView> getViolation(TransactionEntity transaction) {

        return transaction.getViolations().stream().map(violation -> new ViolationView(
                violation.getSeverity(),
                violation.getSource(),
                violation.getTxItemId(),
                violation.getCode(),
                violation.getBag()
        )).collect(Collectors.toSet());
    }

    private BigDecimal getAmountLcy(TransactionEntity tx) {
        BigDecimal total = BigDecimal.ZERO;
        for (val txItem : tx.getItems()) {
            total = total.add(txItem.getAmountLcy());
        }
        return total;
    }

}
