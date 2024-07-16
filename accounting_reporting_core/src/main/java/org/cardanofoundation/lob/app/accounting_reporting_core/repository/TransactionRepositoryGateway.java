package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Rejection;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionRepositoryGateway {

    private final TransactionItemRepository transactionItemRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;

    @Transactional
    public Either<Problem, Boolean> approveTransaction(String transactionId) {
        log.info("Approving transaction: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{transactionId} not found")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        val tx = txM.orElseThrow();

        if (tx.getAutomatedValidationStatus() == FAILED) {
            return Either.left(Problem.builder()
                    .withTitle("CANNOT_APPROVE_FAILED_TX")
                    .withDetail(STR."Cannot approve a failed transaction, transactionId: \{transactionId}")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        tx.setTransactionApproved(true);

        val savedTx = transactionRepository.save(tx);
        val organisationId = savedTx.getOrganisation().getId();

        if (savedTx.getTransactionApproved()) {
            ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, Set.of(savedTx));

            return Either.right(savedTx.getTransactionApproved());
        }

        return Either.right(false);
    }

    @Transactional
    public Set<String> approveTransactions(String organisationId, Set<String> transactionIds) {
        log.info("Approving transactions: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .filter(tx -> tx.getAutomatedValidationStatus() != FAILED)
                .peek(tx -> tx.setTransactionApproved(true))
                .collect(Collectors.toSet());

        val savedTxs = transactionRepository.saveAll(transactions);

        ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, Set.copyOf(savedTxs));

        return savedTxs.stream().map(TransactionEntity::getId).collect(Collectors.toSet());
    }

    @Transactional
    public Set<String> approveTransactionsDispatch(String organisationId, Set<String> transactionIds) {
        log.info("Approving transactions dispatch: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .filter(tx -> tx.getAutomatedValidationStatus() != FAILED)
                .peek(tx -> tx.setLedgerDispatchApproved(true))
                .collect(Collectors.toSet());

        val savedTxs = transactionRepository.saveAll(transactions);

        ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, Set.copyOf(savedTxs));

        return savedTxs.stream().map(TransactionEntity::getId).collect(Collectors.toSet());
    }

    @Transactional
    public Either<Problem, Boolean> approveTransactionDispatch(String transactionId) {
        log.info("Approving transaction dispatch: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{transactionId} not found")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        val tx = txM.orElseThrow();

        if (tx.getAutomatedValidationStatus() == FAILED) {
            return Either.left(Problem.builder()
                    .withTitle("CANNOT_APPROVE_FAILED_TX")
                    .withDetail(STR."Cannot approve dispatch for a failed transaction, transactionId: \{transactionId}")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        tx.setLedgerDispatchApproved(true);

        val savedTx = transactionRepository.save(tx);

        if (savedTx.getLedgerDispatchApproved()) {
            ledgerService.checkIfThereAreTransactionsToDispatch(savedTx.getOrganisation().getId(), Set.of(savedTx));

            return Either.right(savedTx.getLedgerDispatchApproved());
        }

        return Either.right(false);
    }

    public Either<Problem, Boolean> changeTransactionComment(String txId, String userComment) {
        val txM = transactionRepository.findById(txId);

        if (txM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{txId} not found")
                    .with("txId", txId)
                    .build()
            );
        }

        val tx = txM.orElseThrow();

        tx.setUserComment(userComment);

        val savedTx = transactionRepository.save(tx);

        return Either.right(savedTx.getUserComment().equals(userComment));
    }

    public Either<Problem, Boolean> changeTransactionItemRejection(String txId,
                                                                   String txItemId,
                                                                   Optional<Rejection> rejectionM) {
        val txM = transactionRepository.findById(txId);

        if (txM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{txId} not found")
                    .with("txId", txId)
                    .build()
            );
        }

        val tx = txM.orElseThrow();

        val txItemM = tx.findItemById(txItemId);

        if (txItemM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_ITEM_NOT_FOUND")
                    .withDetail(STR."Transaction item with id \{txItemId} not found")
                    .with("txItemId", txItemId)
                    .build()
            );
        }

        val txItem= txItemM.orElseThrow();
        txItem.setRejection(rejectionM.orElse(null));

        val savedTxItem = transactionItemRepository.save(txItem);

        return Either.right(savedTxItem.getRejection().equals(rejectionM));
    }

    public Optional<TransactionEntity> findById(String transactionId) {

        return transactionRepository.findById(transactionId);
    }

    public List<TransactionEntity> findByAllId(Set<String> transactionIds) {

        return transactionRepository.findAllById(transactionIds);
    }

    private static Set<String> transactionIds(Set<TransactionEntity> transactions) {
        return transactions
                .stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());
    }

    public List<TransactionEntity> findAllByStatus(String organisationId,
                                                   List<ValidationStatus> validationStatuses,
                                                   List<TransactionType> transactionType) {
        return transactionRepository.findAllByStatus(organisationId, validationStatuses, transactionType);
    }

    public List<TransactionEntity> listAll() {
        return transactionRepository.findAll();
    }
}
