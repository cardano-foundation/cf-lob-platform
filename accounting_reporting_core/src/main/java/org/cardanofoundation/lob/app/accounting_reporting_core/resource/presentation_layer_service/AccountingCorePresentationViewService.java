package org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.FailureResponses.transactionNotFoundResponse;

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
                .sorted(Comparator.comparing(TransactionView::getAmountTotalLcy).reversed())
                .toList();
    }

    public Optional<TransactionView> transactionDetailSpecific(String transactionId) {
        val transactionEntity = transactionRepositoryGateway.findById(transactionId);

        return transactionEntity.map(this::getTransactionView);
    }

    public Optional<BatchView> batchDetail(String batchId) {
        return transactionBatchRepositoryGateway.findById(batchId).map(transactionBatchEntity -> {
                    val transactions = this.getTransaction(transactionBatchEntity);
                    val statistic = this.getStatistics(transactions);
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

    public BatchsDetailView listAllBatch(BatchSearchRequest body) {
        val batchDetailView = new BatchsDetailView();


        val batches = transactionBatchRepositoryGateway.findByFilter(body)
                .stream()
                .map(
                        transactionBatchEntity -> {
                            val transactions = this.getTransaction(transactionBatchEntity);
                            val statistic = this.getStatistics(transactions);
                            return new BatchView(
                                    transactionBatchEntity.getId(),
                                    transactionBatchEntity.getCreatedAt().toString(),
                                    transactionBatchEntity.getUpdatedAt().toString(),
                                    transactionBatchEntity.getCreatedBy(),
                                    transactionBatchEntity.getUpdatedBy(),
                                    transactionBatchEntity.getOrganisationId(),
                                    transactionBatchEntity.getStatus(),
                                    statistic,
                                    this.getFilteringParameters(transactionBatchEntity.getFilteringParameters()),
                                    Set.of()
                            );
                        }
                ).toList();

        batchDetailView.setBatchs(batches);
        batchDetailView.setTotal(transactionBatchRepositoryGateway.findByFilterCount(body));

        return batchDetailView;
    }

    @Transactional
    public Either<Problem, Void> extractionTrigger(ExtractionRequest body) {
        val fp = UserExtractionParameters.builder()
                .from(LocalDate.parse(body.getDateFrom()))
                .to(LocalDate.parse(body.getDateTo()))
                .organisationId(body.getOrganisationId())
                .transactionTypes(body.getTransactionType())
                .transactionNumbers(body.getTransactionNumbers())
                .build();

        return accountingCoreService.scheduleIngestion(fp);
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public TransactionItemsProcessRejectView rejectTransactionItems(TransactionItemsRejectionRequest transactionItemsRejectionRequest) {


        val txM = transactionRepositoryGateway.findById(transactionItemsRejectionRequest.getTransactionId());
        if (txM.isEmpty()) {
            Either<IdentifiableProblem, TransactionEntity> errorE = transactionNotFoundResponse(transactionItemsRejectionRequest.getTransactionId());
            return TransactionItemsProcessRejectView.createFail(transactionItemsRejectionRequest.getTransactionId(), errorE.getLeft().getProblem());

        }
        val tx = txM.orElseThrow();
        Set<TransactionItemsProcessView> items = transactionRepositoryGateway.rejectTransactionItems(tx, transactionItemsRejectionRequest.getTransactionItemsRejections())
                .stream()
                .map(txItemEntityE -> txItemEntityE.fold(txProblem -> {
                    return TransactionItemsProcessView.createFail(txProblem.getId(), txProblem.getProblem());
                }, success -> {
                    return TransactionItemsProcessView.createSuccess(success.getId());
                }))
                .collect(toSet());

        return TransactionItemsProcessRejectView.createSuccess(
                tx.getId(),
                this.getTransactionDispatchStatus(tx),
                items
        );

    }

    @Transactional
    public BatchReprocessView scheduleReIngestionForFailed(String batchId) {
        val txM = accountingCoreService.scheduleReIngestionForFailed(batchId);

        if (txM.isEmpty()) {
            return BatchReprocessView.createFail(batchId, txM.getLeft());

        }

        return BatchReprocessView.createSuccess(batchId);

    }

    private BatchStatisticsView getStatistics(Set<TransactionView> transactions) {
        val invalid = transactions.stream().filter(transactionView -> INVALID == transactionView.getStatistic()).count();

        val pending = transactions.stream().filter(transactionView -> PENDING == transactionView.getStatistic()).count();

        val approve = transactions.stream().filter(transactionView -> APPROVE == transactionView.getStatistic()).count();

        val publish = transactions.stream().filter(transactionView -> PUBLISH == transactionView.getStatistic()).count();

        val published = transactions.stream().filter(transactionView -> PUBLISHED == transactionView.getStatistic()).count();

        val total = transactions.stream().count();
        return new BatchStatisticsView(
                (int) invalid,
                (int) pending,
                (int) approve,
                (int) publish,
                (int) published,
                (int) total
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


    private Set<TransactionView> getTransaction(TransactionBatchEntity transactionBatchEntity) {
        return transactionBatchEntity.getTransactions().stream()
                .map(this::getTransactionView)
                .sorted(Comparator.comparing(TransactionView::getAmountTotalLcy).reversed())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private TransactionView getTransactionView(TransactionEntity transactionEntity) {
        return new TransactionView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                transactionEntity.getOverallStatus(),
                getTransactionDispatchStatus(transactionEntity),
                transactionEntity.getAutomatedValidationStatus(),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getAmountLcyTotalForAllItems(transactionEntity),
                transactionEntity.hasAnyRejection(),
                getTransactionItemView(transactionEntity),
                getViolations(transactionEntity)

        );
    }


    public LedgerDispatchStatusView getTransactionDispatchStatus(TransactionEntity transactionEntity) {

        if (ValidationStatus.FAILED == transactionEntity.getAutomatedValidationStatus()) {

            if (transactionEntity.getViolations().stream().anyMatch(v -> v.getSource() == ERP)) {
                return INVALID;
            }
            if (transactionEntity.hasAnyRejection()) {
                if (transactionEntity.getItems().stream().anyMatch(transactionItemEntity -> transactionItemEntity.getRejection().stream().anyMatch(rejection -> rejection.getRejectionReason().getSource() == ERP))) {
                    return INVALID;
                }
                return PENDING;
            }
            return PENDING;
        }

        if (transactionEntity.hasAnyRejection()) {
            if (transactionEntity.getItems().stream().anyMatch(transactionItemEntity -> transactionItemEntity.getRejection().stream().anyMatch(rejection -> rejection.getRejectionReason().getSource() == ERP))) {
                return INVALID;
            }
            return PENDING;
        }

        switch (transactionEntity.getLedgerDispatchStatus()) {
            case NOT_DISPATCHED, MARK_DISPATCH -> {
                if (transactionEntity.getLedgerDispatchApproved()) {
                    return PUBLISHED;
                }

                if (transactionEntity.getTransactionApproved()) {
                    return PUBLISH;
                }
            }
            case DISPATCHED, COMPLETED, FINALIZED -> {
                return PUBLISHED;
                //return DISPATCHED;
            }
        }
        return APPROVE;

    }

    private Set<TransactionItemView> getTransactionItemView(TransactionEntity transaction) {
        return transaction.getItems().stream().filter(transactionItemEntity -> {
            return transactionItemEntity.getAmountLcy().compareTo(ZERO) > 0;
        }).map(item -> {
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
                    item.getDocument().flatMap(document -> document.getCounterparty().flatMap(Counterparty::getName)).orElse(""),
                    item.getRejection().map(Rejection::getRejectionReason).orElse(null)
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
                .filter(transactionItemEntity -> {
                    return transactionItemEntity.getAmountLcy().compareTo(ZERO) > 0;
                })
                .map(TransactionItemEntity::getAmountLcy)
                .reduce(ZERO, BigDecimal::add);
    }

}
