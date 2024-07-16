package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;

@Service
@Slf4j
public class TxBatchStatsCalculator {

    public BatchStatistics reCalcStats(TransactionBatchEntity txBatch,
                                       Optional<Integer> totalTransactionsCount) {
        val transactions = txBatch.getTransactions();

        return BatchStatistics.builder()
                .totalTransactionsCount(totalTransactionsCount.orElseGet(() -> txBatch.getBatchStatistics().flatMap(BatchStatistics::getTotalTransactionsCount).orElse(0)))
                .processedTransactionsCount(transactions.size())
                .approvedTransactionsCount(Long.valueOf(transactions.stream().filter(TransactionEntity::getTransactionApproved).count()).intValue())
                .approvedTransactionsDispatchCount(Long.valueOf(transactions.stream().filter(TransactionEntity::getLedgerDispatchApproved).count()).intValue())

                .dispatchedTransactionsCount(Long.valueOf(transactions.stream().filter(tx -> tx.getLedgerDispatchStatus() == DISPATCHED).count()).intValue())
                .completedTransactionsCount(Long.valueOf(transactions.stream().filter(tx -> tx.getLedgerDispatchStatus() == COMPLETED).count()).intValue())
                .finalizedTransactionsCount(Long.valueOf(transactions.stream().filter(tx -> tx.getLedgerDispatchStatus() == FINALIZED).count()).intValue())

                .failedTransactionsCount(Long.valueOf(transactions.stream().filter(tx -> tx.getAutomatedValidationStatus() == FAILED).count()).intValue())

                .failedSourceLOBTransactionsCount(Long.valueOf(txBatch.getTransactions().stream()
                        .filter(tx -> tx.getAutomatedValidationStatus() == FAILED)
                        .map(tx -> tx.getViolations().stream().anyMatch(v -> v.getSource() == LOB)).count()).intValue())

                .failedSourceERPTransactionsCount(Long.valueOf(txBatch.getTransactions().stream()
                        .filter(tx -> tx.getAutomatedValidationStatus() == FAILED)
                        .map(tx -> tx.getViolations().stream().anyMatch(v -> v.getSource() == ERP)).count()).intValue())

                .build();
    }

}
