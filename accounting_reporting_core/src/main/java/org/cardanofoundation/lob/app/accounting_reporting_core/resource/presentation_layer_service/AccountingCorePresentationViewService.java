package org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionRepositoryGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;

@Service
@org.jmolecules.ddd.annotation.Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
// presentation layer service
public class AccountingCorePresentationViewService {

    private final TransactionRepositoryGateway transactionRepositoryGateway;
    private final AccountingCoreService accountingCoreService;
    private final TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;

    public List<TransactionView> allTransactions(SearchRequest body) {
        val transactions = transactionRepositoryGateway.findAllByStatus(
                body.getOrganisationId(),
                body.getStatus(),
                body.getTransactionType()
        );

        return transactions.stream()
                .map(this::getTransactionView)
                .toList();
    }

    public Optional<TransactionView> transactionDetailSpecific(String transactionId) {
        val transactionEntity = transactionRepositoryGateway.findById(transactionId);

        return transactionEntity.map(this::getTransactionView);
    }

    public Optional<BatchView> batchDetail(String batchId) {
        return transactionBatchRepositoryGateway.findById(batchId).map(transactionBatchEntity -> {
                    val transactions = this.getTransaction(transactionBatchEntity);
                    val statistic = this.getStatistics(transactionBatchEntity.getBatchStatistics());
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

    private BatchStatisticsView getStatistics(Optional<BatchStatistics> batchStatistics) {
        val statisticsM = batchStatistics.stream().findFirst();

        return new BatchStatisticsView(
                statisticsM.flatMap(BatchStatistics::getApprovedTransactionsCount).orElse(0),
                Math.abs(statisticsM.flatMap(BatchStatistics::getTotalTransactionsCount).orElse(0) - statisticsM.flatMap(BatchStatistics::getProcessedTransactionsCount).orElse(0)),
                statisticsM.flatMap(BatchStatistics::getFailedTransactionsCount).orElse(0),
                statisticsM.flatMap(BatchStatistics::getDispatchedTransactionsCount).orElse(0),
                statisticsM.flatMap(BatchStatistics::getCompletedTransactionsCount).orElse(0),
                statisticsM.flatMap(BatchStatistics::getTotalTransactionsCount).orElse(0)
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
        val batchDetailView = new BatchsDetailView();

        val batches = transactionBatchRepositoryGateway.findByFilter(body)
                .stream()
                .map(
                        transactionBatchEntity -> new BatchView(
                                transactionBatchEntity.getId(),
                                transactionBatchEntity.getCreatedAt().toString(),
                                transactionBatchEntity.getUpdatedAt().toString(),
                                transactionBatchEntity.getCreatedBy(),
                                transactionBatchEntity.getUpdatedBy(),
                                transactionBatchEntity.getOrganisationId(),
                                transactionBatchEntity.getStatus(),
                                this.getStatistics(transactionBatchEntity.getBatchStatistics()),
                                this.getFilteringParameters(transactionBatchEntity.getFilteringParameters()),
                                Set.of()
                        )
                ).toList();

        batchDetailView.setBatchs(batches);
        batchDetailView.setTotal(transactionBatchRepositoryGateway.findByFilterCount(body));

        return batchDetailView;
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

    public List<TransactionProcessView> approveTransactions(TransactionsRequest transactionsRequest) {
        return transactionRepositoryGateway.approveTransactions(transactionsRequest)
                .stream()
                .map(txEntityE -> txEntityE.fold(txProblem -> {
                    return TransactionProcessView.createFail(txProblem.getId(), txProblem.getProblem());
                }, success -> {
                    return TransactionProcessView.createSuccess(success.getId());
                }))
                .toList();
    }

    public List<TransactionProcessView> approveTransactionsPublish(TransactionsRequest transactionsRequest) {
        return transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest)
                .stream()
                .map(txEntityE -> txEntityE.fold(txProblem -> {
                    return TransactionProcessView.createFail(txProblem.getId(), txProblem.getProblem());
                }, success -> {
                    return TransactionProcessView.createSuccess(success.getId());
                }))
                .toList();
    }

    public List<TransactionItemsProcessView> rejectTransactionItems(TransactionItemsRejectionRequest transactionItemsRejectionRequest) {
        return transactionRepositoryGateway.rejectTransactionItems(transactionItemsRejectionRequest)
                .stream()
                .map(txItemEntityE -> txItemEntityE.fold(txProblem -> {
                    return TransactionItemsProcessView.createFail(txProblem.getId(), txProblem.getProblem());
                }, success -> {
                    return TransactionItemsProcessView.createSuccess(success.getId());
                }))
                .toList();
    }

    private Set<TransactionView> getTransaction(TransactionBatchEntity transactionBatchEntity) {
        return transactionBatchEntity.getTransactions().stream()
                .map(this::getTransactionView)
                .collect(toSet());
    }

    private TransactionView getTransactionView(TransactionEntity transactionEntity) {
        return new TransactionView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                transactionEntity.getStatus(),
                getTransactionDispatchStatus(transactionEntity),
                transactionEntity.getAutomatedValidationStatus(),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getAmountLcyTotalForAllItems(transactionEntity),
                getTransactionItemView(transactionEntity),
                getViolations(transactionEntity)

        );
    }


    public LedgerDispatchStatusView getTransactionDispatchStatus(TransactionEntity transactionEntity) {

        if (TransactionStatus.FAIL == transactionEntity.getStatus()) {
            return LedgerDispatchStatusView.INVALID;
        }

        switch (transactionEntity.getLedgerDispatchStatus()) {
            case MARK_DISPATCH -> {
                return LedgerDispatchStatusView.APPROVE;
            }
            case NOT_DISPATCHED -> {
                return LedgerDispatchStatusView.PENDING;
            }
            case DISPATCHED -> {
                return LedgerDispatchStatusView.PUBLISH;
            }
            case COMPLETED,FINALIZED -> {
                return LedgerDispatchStatusView.PUBLISHED;
            }
        }
        return LedgerDispatchStatusView.INVALID;

    }

    private Set<TransactionItemView> getTransactionItemView(TransactionEntity transaction) {
        return transaction.getItems().stream().map(item -> {
            return new TransactionItemView(
                    item.getId(),
                    item.getAccountDebit().map(Account::getCode).orElse(""),
                    item.getAccountDebit().flatMap(Account::getName).orElse(""),
                    item.getAccountDebit().flatMap(Account::getRefCode).orElse(""),
                    item.getAccountCredit().map(Account::getCode).orElse(""),
                    item.getAccountCredit().flatMap(Account::getName).orElse(""),
                    item.getAccountCredit().flatMap(Account::getRefCode).orElse(""),
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
                    item.getDocument().flatMap(document -> document.getVat().flatMap(Vat::getRate)).orElse(ZERO),
                    item.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getCustomerCode)).orElse(""),
                    item.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getType)).orElse(VENDOR),
                    item.getDocument().flatMap(document -> document.getCounterparty().flatMap(Counterparty::getName)).orElse("")
            );
        }).collect(toSet());
    }

    private Set<ViolationView> getViolations(TransactionEntity transaction) {
        return transaction.getViolations().stream().map(violation -> new ViolationView(
                violation.getSeverity(),
                violation.getSource(),
                violation.getTxItemId(),
                violation.getCode(),
                violation.getBag()
        )).collect(toSet());
    }

    public BigDecimal getAmountLcyTotalForAllItems(TransactionEntity tx) {
        return tx.getItems().stream()
                .map(TransactionItemEntity::getAmountLcy)
                .reduce(ZERO, BigDecimal::add);
    }

}
