package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.FAILED;

import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

@Service
@Slf4j
public class TxBatchStatsCalculator {

    public BatchStatistics reCalcStats(TransactionBatchEntity txBatch,
                                       Optional<Integer> totalTransactionsCount) {
        Set<TransactionEntity> transactions = txBatch.getTransactions();

        return BatchStatistics.builder()
                .totalTransactionsCount(totalTransactionsCount.orElseGet(() -> txBatch.getBatchStatistics().flatMap(BatchStatistics::getTotalTransactionsCount).orElse(0)))
                .processedTransactionsCount(transactions.size())
                .approvedTransactionsCount((int) transactions.stream().filter(TransactionEntity::getTransactionApproved).count())
                .approvedTransactionsDispatchCount((int) transactions.stream().filter(TransactionEntity::getLedgerDispatchApproved).count())

                .dispatchedTransactionsCount((int) transactions.stream().filter(tx -> tx.getLedgerDispatchStatus() == DISPATCHED).count())
                .completedTransactionsCount((int) transactions.stream().filter(tx -> tx.getLedgerDispatchStatus() == COMPLETED).count())
                .finalizedTransactionsCount((int) transactions.stream().filter(tx -> tx.getLedgerDispatchStatus() == FINALIZED).count())

                .failedTransactionsCount((int) transactions.stream().filter(tx -> tx.getAutomatedValidationStatus() == FAILED).count())

                .failedSourceLOBTransactionsCount((int) txBatch.getTransactions().stream()
                        .filter(tx -> tx.getAutomatedValidationStatus() == FAILED)
                        .map(tx -> tx.getViolations().stream().anyMatch(v -> v.getSource() == LOB)).count())

                .failedSourceERPTransactionsCount((int) txBatch.getTransactions().stream()
                        .filter(tx -> tx.getAutomatedValidationStatus() == FAILED)
                        .map(tx -> tx.getViolations().stream().anyMatch(v -> v.getSource() == ERP)).count())

                .build();
    }

}
