package org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.FailureResponses.transactionNotFoundResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.vavr.control.Either;
import org.zalando.problem.Problem;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.reconcilation.ReconcilationEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.reconcilation.ReconcilationViolation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreTransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionReconcilationRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;

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
    private final TransactionReconcilationRepository transactionReconcilationRepository;

    /**
     * TODO: waiting for refactoring the layer to remove this
     */
    private final AccountingCoreTransactionRepository accountingCoreTransactionRepository;

    public ReconciliationResponseView allReconciliationTransaction(ReconciliationFilterRequest body) {
        Object transactionsStatistic = accountingCoreTransactionRepository.findCalcReconciliationStatistic();
        Optional<ReconcilationEntity> latestReconcilation = transactionReconcilationRepository.findTopByOrderByCreatedAtDesc();
        Set<TransactionReconciliationTransactionsView> transactions;
        if (body.getFilter().equals(ReconciliationFilterStatusRequest.UNRECONCILED)) {
            Set<Object> txDuplicated = new HashSet<>();
            transactions = accountingCoreTransactionRepository.findAllReconciliationSpecial(body.getReconciliationRejectionCode(), body.getDateFrom(), body.getLimit(), body.getPage()).stream()
                    .filter(o -> {
                        if (o[0] instanceof TransactionEntity transactionEntity && !txDuplicated.contains((transactionEntity).getId())) {
                                txDuplicated.add((transactionEntity).getId());
                                return true;
                            }

                        if (o[1] instanceof ReconcilationViolation reconcilationViolation && !txDuplicated.contains((reconcilationViolation).getTransactionId())) {
                                txDuplicated.add((reconcilationViolation).getTransactionId());
                                return true;
                            }

                        return false;
                    })
                    .map(this::getReconciliationTransactionsSelector)
                    .sorted(Comparator.comparing(TransactionReconciliationTransactionsView::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            transactions = transactionRepositoryGateway.findReconciliation(body.getFilter(), body.getLimit(), body.getPage()).stream()
                    .map(this::getTransactionReconciliationView)
                    .collect(toSet());
        }
        return new ReconciliationResponseView(
                (long) transactions.size(),
                latestReconcilation.flatMap(ReconcilationEntity::getFrom),
                latestReconcilation.flatMap(ReconcilationEntity::getTo),
                latestReconcilation.map(reconcilationEntity -> reconcilationEntity.getUpdatedAt().toLocalDate()),
                getTransactionReconciliationStatistic(transactionsStatistic),
                transactions
        );
    }

    public List<TransactionView> allTransactions(SearchRequest body) {
        List<TransactionEntity> transactions = transactionRepositoryGateway.findAllByStatus(
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
        Optional<TransactionEntity> transactionEntity = transactionRepositoryGateway.findById(transactionId);

        return transactionEntity.map(this::getTransactionView);
    }

    public Optional<BatchView> batchDetail(String batchId) {
        return transactionBatchRepositoryGateway.findById(batchId).map(transactionBatchEntity -> {
            Set<TransactionView> transactions = this.getTransaction(transactionBatchEntity);
            BatchStatisticsView statistic = this.getBatchesStatistics(transactions);
            FilteringParametersView filteringParameters = this.getFilteringParameters(transactionBatchEntity.getFilteringParameters());

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
        BatchsDetailView batchDetailView = new BatchsDetailView();

        List<BatchView> batches = transactionBatchRepositoryGateway.findByFilter(body)
                .stream()
                .map(
                        transactionBatchEntity -> {
                            Set<TransactionView> transactions = this.getTransaction(transactionBatchEntity);
                            BatchStatisticsView statistic = this.getBatchesStatistics(transactions);
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
        ArrayList<String> transactionNumbers = new ArrayList<>(body.getTransactionNumbers());
        transactionNumbers.removeIf(String::isEmpty);

        UserExtractionParameters fp = UserExtractionParameters.builder()
                .from(LocalDate.parse(body.getDateFrom()))
                .to(LocalDate.parse(body.getDateTo()))
                .organisationId(body.getOrganisationId())
                .transactionTypes(body.getTransactionType())
                .transactionNumbers(transactionNumbers)
                .build();

        return accountingCoreService.scheduleIngestion(fp);
    }

    @Transactional
    public List<TransactionProcessView> approveTransactions(TransactionsRequest transactionsRequest) {
        return transactionRepositoryGateway.approveTransactions(transactionsRequest)
                .stream()
                .map(txEntityE -> txEntityE.fold(
                        txProblem -> TransactionProcessView.createFail(txProblem.getId(), txProblem.getProblem()),
                        success -> TransactionProcessView.createSuccess(success.getId())
                ))
                .toList();
    }

    @Transactional
    public List<TransactionProcessView> approveTransactionsPublish(TransactionsRequest transactionsRequest) {
        return transactionRepositoryGateway.approveTransactionsDispatch(transactionsRequest)
                .stream()
                .map(txEntityE -> txEntityE.fold(
                        txProblem -> TransactionProcessView.createFail(txProblem.getId(), txProblem.getProblem()),
                        success -> TransactionProcessView.createSuccess(success.getId())
                ))
                .toList();
    }

    @Transactional
    public TransactionItemsProcessRejectView rejectTransactionItems(TransactionItemsRejectionRequest transactionItemsRejectionRequest) {
        Optional<TransactionEntity> txM = transactionRepositoryGateway.findById(transactionItemsRejectionRequest.getTransactionId());
        if (txM.isEmpty()) {
            Either<IdentifiableProblem, TransactionEntity> errorE = transactionNotFoundResponse(transactionItemsRejectionRequest.getTransactionId());
            return TransactionItemsProcessRejectView.createFail(transactionItemsRejectionRequest.getTransactionId(), errorE.getLeft().getProblem());

        }
        TransactionEntity tx = txM.orElseThrow();
        Set<TransactionItemsProcessView> items = transactionRepositoryGateway.rejectTransactionItems(tx, transactionItemsRejectionRequest.getTransactionItemsRejections())
                .stream()
                .map(txItemEntityE -> txItemEntityE.fold(txProblem -> TransactionItemsProcessView.createFail(txProblem.getId(), txProblem.getProblem())
                , success -> TransactionItemsProcessView.createSuccess(success.getId())
                ))
                .collect(toSet());

        return TransactionItemsProcessRejectView.createSuccess(
                tx.getId(),
                this.getTransactionDispatchStatus(tx),
                items
        );
    }

    @Transactional
    public BatchReprocessView scheduleReIngestionForFailed(String batchId) {
        Either<Problem, Void> txM = accountingCoreService.scheduleReIngestionForFailed(batchId);

        if (txM.isEmpty()) {
            return BatchReprocessView.createFail(batchId, txM.getLeft());

        }

        return BatchReprocessView.createSuccess(batchId);
    }

    private BatchStatisticsView getBatchesStatistics(Set<TransactionView> transactions) {
        long invalid = transactions.stream().filter(transactionView -> INVALID == transactionView.getStatistic()).count();

        long pending = transactions.stream().filter(transactionView -> PENDING == transactionView.getStatistic()).count();

        long approve = transactions.stream().filter(transactionView -> APPROVE == transactionView.getStatistic()).count();

        long publish = transactions.stream().filter(transactionView -> PUBLISH == transactionView.getStatistic()).count();

        long published = transactions.stream().filter(transactionView -> PUBLISHED == transactionView.getStatistic()).count();

        long total = transactions.size();

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

    private TransactionReconciliationTransactionsView getTransactionReconciliationView(TransactionEntity transactionEntity) {
        return new TransactionReconciliationTransactionsView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                DataSourceView.NETSUITE,
                Optional.of(transactionEntity.getOverallStatus()),
                Optional.of(getTransactionDispatchStatus(transactionEntity)),
                Optional.of(transactionEntity.getAutomatedValidationStatus()),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getAmountLcyTotalForAllItems(transactionEntity),
                false,
                transactionEntity.getReconcilation().flatMap(reconcilation -> reconcilation.getSource().map(TransactionReconciliationTransactionsView.ReconciliationCodeView::of))
                        .orElse(TransactionReconciliationTransactionsView.ReconciliationCodeView.NEVER),
                transactionEntity.getReconcilation().flatMap(reconcilation -> reconcilation.getSink().map(TransactionReconciliationTransactionsView.ReconciliationCodeView::of))
                        .orElse(TransactionReconciliationTransactionsView.ReconciliationCodeView.NEVER),
                transactionEntity.getReconcilation().flatMap(reconcilation -> reconcilation.getFinalStatus().map(TransactionReconciliationTransactionsView.ReconciliationCodeView::of))
                        .orElse(TransactionReconciliationTransactionsView.ReconciliationCodeView.NEVER),

                transactionEntity.getLastReconcilation().map(reconcilationEntity -> reconcilationEntity.getViolations().stream()
                            .filter(reconcilationViolation -> reconcilationViolation.getTransactionId().equals(transactionEntity.getId()))
                            .map(reconcilationViolation -> ReconciliationRejectionCodeRequest.of(reconcilationViolation.getRejectionCode(), transactionEntity.getLedgerDispatchApproved()))
                            .collect(toSet()))
                        .orElse(new LinkedHashSet<>()),
                transactionEntity.getLastReconcilation().map(CommonEntity::getCreatedAt).orElse(null),
                getTransactionItemView(transactionEntity),
                getViolations(transactionEntity)

        );
    }

    private TransactionReconciliationTransactionsView getTransactionReconciliationViolationView(ReconcilationViolation reconcilationViolation) {
        return new TransactionReconciliationTransactionsView(
                reconcilationViolation.getTransactionId(),
                reconcilationViolation.getTransactionInternalNumber(),
                reconcilationViolation.getTransactionEntryDate(),
                reconcilationViolation.getTransactionType(),
                DataSourceView.NETSUITE,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                false,
                reconcilationViolation.getAmountLcySum(),
                false,
                TransactionReconciliationTransactionsView.ReconciliationCodeView.NOK,
                TransactionReconciliationTransactionsView.ReconciliationCodeView.NOK,
                TransactionReconciliationTransactionsView.ReconciliationCodeView.NOK,
                Set.of(ReconciliationRejectionCodeRequest.of(reconcilationViolation.getRejectionCode(), false)),
                null,
                new LinkedHashSet<>(),
                new LinkedHashSet<>()

        );
    }

    private TransactionReconciliationTransactionsView getTransactionReconciliationViolationView() {
        return new TransactionReconciliationTransactionsView(
                "",
                "",
                LocalDate.now(),
                TransactionType.CardCharge,
                DataSourceView.NETSUITE,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                false,
                BigDecimal.valueOf(123),
                false,
                TransactionReconciliationTransactionsView.ReconciliationCodeView.NOK,
                TransactionReconciliationTransactionsView.ReconciliationCodeView.NOK,
                TransactionReconciliationTransactionsView.ReconciliationCodeView.NOK,
                new LinkedHashSet<>(),
                null,
                null,
                null

        );
    }

    private TransactionView getTransactionView(TransactionEntity transactionEntity) {
        return new TransactionView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                DataSourceView.NETSUITE,
                transactionEntity.getOverallStatus(),
                getTransactionDispatchStatus(transactionEntity),
                transactionEntity.getAutomatedValidationStatus(),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getAmountLcyTotalForAllItems(transactionEntity),
                transactionEntity.hasAnyRejection(),
                transactionEntity.getReconcilation().flatMap(reconcilation -> reconcilation.getSource().map(TransactionView.ReconciliationCodeView::of))
                        .orElse(TransactionView.ReconciliationCodeView.NEVER),
                transactionEntity.getReconcilation().flatMap(reconcilation -> reconcilation.getSink().map(TransactionView.ReconciliationCodeView::of))
                        .orElse(TransactionView.ReconciliationCodeView.NEVER),
                transactionEntity.getReconcilation().flatMap(reconcilation -> reconcilation.getFinalStatus().map(TransactionView.ReconciliationCodeView::of))
                        .orElse(TransactionView.ReconciliationCodeView.NEVER),

                transactionEntity.getLastReconcilation().map(reconcilationEntity -> reconcilationEntity.getViolations().stream()
                            .filter(reconcilationViolation -> reconcilationViolation.getTransactionId().equals(transactionEntity.getId()))
                            .map(reconcilationViolation -> ReconciliationRejectionCodeRequest.of(reconcilationViolation.getRejectionCode(), transactionEntity.getLedgerDispatchApproved()))
                            .collect(toSet()))
                        .orElse(new LinkedHashSet<>()),
                transactionEntity.getLastReconcilation().map(CommonEntity::getCreatedAt).orElse(null),
                getTransactionItemView(transactionEntity),
                getViolations(transactionEntity)

        );
    }


    public LedgerDispatchStatusView getTransactionDispatchStatus(TransactionEntity transactionEntity) {
        if (FAILED == transactionEntity.getAutomatedValidationStatus()) {
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
                if (Boolean.TRUE.equals(transactionEntity.getLedgerDispatchApproved())) {
                    return PUBLISHED;
                }

                if (Boolean.TRUE.equals(transactionEntity.getTransactionApproved())) {
                    return PUBLISH;
                }
            }

            case DISPATCHED, COMPLETED, FINALIZED -> {
                return PUBLISHED;
            }
        }

        return APPROVE;
    }

    private TransactionReconciliationStatisticView getTransactionReconciliationStatistic(Object transactionsStatistic) {

        Object[] result = (Object[]) transactionsStatistic;
        return new TransactionReconciliationStatisticView(
                (Integer) ((Long) result[0]).intValue(),
                (Integer) ((Long) result[1]).intValue(),
                (Integer) ((Long) result[2]).intValue(),
                (Integer) ((Long) result[3]).intValue(),
                (Integer) ((Long) result[4]).intValue(),
                (Long) result[5],
                (Integer) ((Long) result[6]).intValue(),
                (Long) result[7],
                (Integer) (((Long) result[5]).intValue() + ((Long) result[6]).intValue())
        );
    }

    private Set<TransactionItemView> getTransactionItemView(TransactionEntity transaction) {
        return transaction.getItems().stream().map(item -> new TransactionItemView(
                    item.getId(),
                    item.getAccountDebit().map(Account::getCode).orElse(""),
                    item.getAccountDebit().flatMap(Account::getName).orElse(""),
                    item.getAccountDebit().flatMap(Account::getRefCode).orElse(""),
                    item.getAccountCredit().map(Account::getCode).orElse(""),
                    item.getAccountCredit().flatMap(Account::getName).orElse(""),
                    item.getAccountCredit().flatMap(Account::getRefCode).orElse(""),
                    item.getAmountFcy().abs(),
                    item.getAmountLcy().abs(),
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
                    item.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getType)).isPresent() ? item.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getType)).get().toString() : "",
                    item.getDocument().flatMap(document -> document.getCounterparty().flatMap(Counterparty::getName)).orElse(""),
                    item.getRejection().map(Rejection::getRejectionReason).orElse(null)
            )).collect(toSet());
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

    private TransactionReconciliationTransactionsView getReconciliationTransactionsSelector(Object[] violations) {
        for (Object o : violations) {
            if (o instanceof TransactionEntity transactionEntity && transactionEntity.getLastReconcilation().isPresent()) {
                return getTransactionReconciliationView(transactionEntity);
            }
            if (o instanceof ReconcilationViolation reconcilationViolation) {
                return getTransactionReconciliationViolationView(reconcilationViolation);
            }

            try {
                log.warn("Object type: {}", o.getClass());
            } catch (Exception e) {
                log.warn("\nempty object: {}\n", o);
            }

        }

        return getTransactionReconciliationViolationView();
    }

    public BigDecimal getAmountLcyTotalForAllItems(TransactionEntity tx) {
        Set<TransactionItemEntity> items = tx.getItems();

        if (tx.getTransactionType().equals(TransactionType.Journal)) {
            items = tx.getItems().stream().filter(txItems -> txItems.getOperationType().equals(Optional.of(OperationType.DEBIT))).collect(toSet());
        }

        return items.stream()
                .map(TransactionItemEntity::getAmountLcy)
                .reduce(ZERO, BigDecimal::add).abs();
    }

}
