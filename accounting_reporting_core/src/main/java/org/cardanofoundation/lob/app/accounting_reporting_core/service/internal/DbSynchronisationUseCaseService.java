package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import static java.util.stream.Collectors.toMap;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode.TX_VERSION_CONFLICT_TX_NOT_MODIFIABLE;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.WARN;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionViolation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreTransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;

@Service
@Slf4j
@RequiredArgsConstructor
public class DbSynchronisationUseCaseService {

    private final AccountingCoreTransactionRepository accountingCoreTransactionRepository;
    private final TransactionConverter transactionConverter;
    private final TransactionItemRepository transactionItemRepository;
    private final TransactionBatchAssocRepository transactionBatchAssocRepository;
    private final TransactionBatchService transactionBatchService;

    @Transactional
    public void execute(String batchId,
                        OrganisationTransactions incomingTransactions,
                        int totalTransactionsCount,
                        ProcessorFlags flags) {
        val trigger = flags.getTrigger();
        val transactions = incomingTransactions.transactions();

        if (transactions.isEmpty()) {
            log.info("No transactions to process, batchId: {}", batchId);
            transactionBatchService.updateTransactionBatchStatusAndStats(batchId, Optional.of(totalTransactionsCount));

            return;
        }

        if (trigger == ProcessorFlags.Trigger.REPROCESSING) {
            // TODO should we check if we are NOT changing incomingTransactions which are already marked as dispatched?
            storeTransactions(batchId, incomingTransactions, flags);
            return;
        }

        val organisationId = incomingTransactions.organisationId();

        processTransactionsForTheFirstTime(batchId, organisationId, transactions, Optional.of(totalTransactionsCount), flags);
    }

    @Transactional
    private void processTransactionsForTheFirstTime(String batchId,
                                                    String organisationId,
                                                    Set<TransactionEntity> incomingDetachedTransactions,
                                                    Optional<Integer> totalTransactionsCount,
                                                    ProcessorFlags flags) {
        val trigger = flags.getTrigger();
        val txsAlreadyStored = new LinkedHashSet<TransactionEntity>();

        val txIds = incomingDetachedTransactions.stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());

        val databaseTransactionsMap = accountingCoreTransactionRepository.findAllById(txIds)
                .stream()
                .collect(toMap(TransactionEntity::getId, Function.identity()));

        val toProcessTransactions = new LinkedHashSet<TransactionEntity>();

        for (val incomingTx : incomingDetachedTransactions) {
            val txM = Optional.ofNullable(databaseTransactionsMap.get(incomingTx.getId()));

            val isDispatchMarked = txM.map(TransactionEntity::allApprovalsPassedForTransactionDispatch).orElse(false);
            val notStoredYet = txM.isEmpty();
            /** If is a new transaction || the new one is different from our Db copy || the transaction has an ERP source violation || transaction item has an ERP source rejection -> then should be processed*/
            val isChanged = notStoredYet || (txM.map(tx -> !isIncomingTransactionERPSame(tx, incomingTx) || tx.hasAnyRejection(Source.ERP) || tx.hasAnyViolation(Source.ERP)).orElse(false));

            if (isDispatchMarked && isChanged) {
                log.warn("Transaction cannot be altered, it is already marked as dispatched, transactionNumber: {}", incomingTx.getTransactionInternalNumber());
                txsAlreadyStored.add(incomingTx);
            }

            if (isChanged && !isDispatchMarked) {
                if (txM.isPresent()) {
                    val attached = txM.orElseThrow();

                    transactionConverter.copyFields(attached, incomingTx);
                    attached.getAllItems().clear();
                    attached.getAllItems().addAll(incomingTx.getAllItems());
                    toProcessTransactions.add(attached);
                } else {
                    toProcessTransactions.add(incomingTx);
                }
            }
        }

        raiseViolationForAlreadyProcessedTransactions(txsAlreadyStored);

        storeTransactions(batchId, new OrganisationTransactions(organisationId, toProcessTransactions), flags);

        transactionBatchService.updateTransactionBatchStatusAndStats(batchId, totalTransactionsCount);
    }

    private void storeTransactions(String batchId,
                                   OrganisationTransactions transactions,
                                   ProcessorFlags flags) {
        log.info("Updating transaction batch, batchId: {}", batchId);
        val trigger = flags.getTrigger();
        val txs = transactions.transactions();

        for (val tx : txs) {
            val saved = accountingCoreTransactionRepository.save(tx);
            saved.getAllItems().forEach(i -> i.setTransaction(saved));

            /** Remove items rejection according to the processor selected */
            if (trigger == ProcessorFlags.Trigger.IMPORT) {
                tx.clearAllItemsRejectionsSource(Source.ERP);
            }
            if (trigger == ProcessorFlags.Trigger.REPROCESSING) {
                tx.clearAllItemsRejectionsSource(Source.LOB);
            }

            transactionItemRepository.saveAll(tx.getAllItems());
        }

        val transactionBatchAssocEntities = txs
                .stream()
                .map(tx -> {
                    val id = new TransactionBatchAssocEntity.Id(batchId, tx.getId());

                    return transactionBatchAssocRepository.findById(id).orElseGet(() -> new TransactionBatchAssocEntity(id));
                })
                .collect(Collectors.toSet());

        transactionBatchAssocRepository.saveAll(transactionBatchAssocEntities);
    }

    private boolean isIncomingTransactionERPSame(TransactionEntity existingTx,
                                                 TransactionEntity incomingTx) {
        val existingTxVersion = ERPSourceTransactionVersionCalculator.compute(existingTx);
        val incomingTxVersion = ERPSourceTransactionVersionCalculator.compute(incomingTx);

        log.info("Existing transaction version:{}, incomingTx:{}", existingTxVersion, incomingTxVersion);

        return existingTxVersion.equals(incomingTxVersion);
    }

    // TODO we are breaking the rule here that violations are only raised in business rules code (e.g. business rules task items)
    private void raiseViolationForAlreadyProcessedTransactions(Set<TransactionEntity> txsAlreadyDispatched) {
        if (txsAlreadyDispatched.isEmpty()) {
            return;
        }

        log.info("txs causing conflict count:{}", txsAlreadyDispatched.size());

        for (val tx : txsAlreadyDispatched) {
            log.info("tx causing conflict: {}", tx);

            val v = TransactionViolation.builder()
                    .code(TX_VERSION_CONFLICT_TX_NOT_MODIFIABLE)
                    .severity(WARN)
                    .source(Source.ERP)
                    .processorModule(this.getClass().getSimpleName())
                    .bag(
                            Map.of(
                                    "transactionNumber", tx.getTransactionInternalNumber()
                            )
                    )
                    .build();

            tx.addViolation(v);
        }
    }

}
