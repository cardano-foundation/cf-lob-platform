package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionBatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.VALIDATED;

@Service
@Slf4j
public class TxBatchStatusCalculator {

    public TransactionBatchStatus reCalcStatus(TransactionBatchEntity transactionBatchEntity,
                                               Optional<Integer> totalTransactionsCount) {
        val allBatchTransactions = transactionBatchEntity.getTransactions();

        val validTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.getAutomatedValidationStatus() == VALIDATED)
                .count();

        val dispatchedTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.getLedgerDispatchStatus() == DISPATCHED)
                .count();

        val completedTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.getLedgerDispatchStatus() == COMPLETED)
                .count();

        val finalisedTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.getLedgerDispatchStatus() == FINALIZED)
                .count();

        if (dispatchedTransactionsCount == validTransactionsCount) {
            return TransactionBatchStatus.FINISHED;
        }
        if (completedTransactionsCount == validTransactionsCount) {
            return TransactionBatchStatus.COMPLETE;
        }
        if (finalisedTransactionsCount == validTransactionsCount) {
            return TransactionBatchStatus.FINALIZED;
        }

        return TransactionBatchStatus.PROCESSING;
    }

}
