package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Rejection;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionsRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionsRequest.TransactionId;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.LedgerService;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.FAIL;
import static org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem.IdType.TRANSACTION;
import static org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem.IdType.TRANSACTION_ITEM;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.zalando.problem.Status.METHOD_NOT_ALLOWED;
import static org.zalando.problem.Status.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionRepositoryGateway {

    private final TransactionItemRepository transactionItemRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;

    @Transactional(propagation = REQUIRES_NEW)
    // TODO optimise performance because we have to load transaction from db each time and we don't save it in bulk
    private Either<IdentifiableProblem, TransactionEntity> approveTransaction(String transactionId) {
        log.info("Approving transaction: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            return txNotFoundResponse(transactionId);
        }

        val tx = txM.orElseThrow();

        if (tx.getStatus() == FAIL) {
            return transactionFailedResponse(transactionId);
        }
        if (tx.hasAnyRejection()) {
            return transactionRejectedResponse(transactionId);
        }

        tx.setTransactionApproved(true);

        val savedTx = transactionRepository.save(tx);

        return Either.right(savedTx);
    }

    @Transactional(propagation = REQUIRES_NEW)
    // TODO optimise performance because we have to load transaction from db each time and we don't save it in bulk
    private Either<IdentifiableProblem, TransactionEntity> approveTransactionsDispatch(String transactionId) {
        log.info("Approving transaction to dispatch: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            val problem = Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{transactionId} not found")
                    .withStatus(NOT_FOUND)
                    .with("transactionId", transactionId)
                    .build();

            return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
        }

        val tx = txM.orElseThrow();

        if (tx.getStatus() == FAIL) {
            val problem = Problem.builder()
                    .withTitle("CANNOT_APPROVE_FAILED_TX")
                    .withDetail(STR."Cannot approve a failed transaction, transactionId: \{transactionId}")
                    .withStatus(METHOD_NOT_ALLOWED)
                    .with("transactionId", transactionId)
                    .build();

            if (tx.hasAnyRejection()) {
                return transactionRejectedResponse(transactionId);
            }

            return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
        }

        if (!tx.getTransactionApproved()) {
            val problem = Problem.builder()
                    .withTitle("TX_NOT_APPROVED")
                    .withDetail(STR."Cannot approve for dispatch / publish a transaction that has not been approved before, transactionId: \{transactionId}")
                    .withStatus(METHOD_NOT_ALLOWED)
                    .with("transactionId", transactionId)
                    .build();

            return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
        }

        tx.setLedgerDispatchApproved(true);

        val savedTx = transactionRepository.save(tx);

        return Either.right(savedTx);
    }

    public List<Either<IdentifiableProblem, TransactionEntity>> approveTransactions(TransactionsRequest transactionsRequest) {
        val organisationId = transactionsRequest.getOrganisationId();

        val transactionIds = transactionsRequest.getTransactionIds()
                .stream()
                .map(TransactionId::getId)
                .collect(Collectors.toSet());

        val transactionsApprovalResponseListE = new ArrayList<Either<IdentifiableProblem, TransactionEntity>>();
        for (val transactionId : transactionIds) {
            try {
                val transactionEntities = approveTransaction(transactionId);

                transactionsApprovalResponseListE.add(transactionEntities);
            } catch (DataAccessException dae) {
                log.error("Error approving transaction: {}", transactionId, dae);

                val problem = createDBError(transactionId, dae);

                transactionsApprovalResponseListE.add(Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION)));
            }
        }

        val transactionSuccesses = transactionsApprovalResponseListE.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .collect(Collectors.toSet());

        ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, transactionSuccesses);

        return transactionsApprovalResponseListE;
    }

    public List<Either<IdentifiableProblem, TransactionEntity>> approveTransactionsDispatch(TransactionsRequest transactionsRequest) {
        val organisationId = transactionsRequest.getOrganisationId();

        val transactionIds = transactionsRequest.getTransactionIds()
                .stream()
                .map(TransactionId::getId)
                .collect(Collectors.toSet());

        val transactionsApprovalResponseListE = new ArrayList<Either<IdentifiableProblem, TransactionEntity>>();
        for (val transactionId : transactionIds) {
            try {
                val transactionEntities = approveTransactionsDispatch(transactionId);

                transactionsApprovalResponseListE.add(transactionEntities);
            } catch (DataAccessException dae) {
                log.error("Error approving transaction publish: {}", transactionId, dae);

                val problem = createDBError(transactionId, dae);

                transactionsApprovalResponseListE.add(Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION)));
            }
        }

        val transactionSuccesses = transactionsApprovalResponseListE.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .collect(Collectors.toSet());

        ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, transactionSuccesses);

        return transactionsApprovalResponseListE;
    }

    public Either<IdentifiableProblem, TransactionEntity> rejectTransaction(String txId,
                                                                            String txItemId,
                                                                            Optional<Rejection> rejectionM) {
        val txM = transactionRepository.findById(txId);

        if (txM.isEmpty()) {
            return txNotFoundResponse(txId);
        }

        val tx = txM.orElseThrow();

        val txItemM = tx.findItemById(txItemId);

        if (txItemM.isEmpty()) {
            val problem = Problem.builder()
                    .withTitle("TX_ITEM_NOT_FOUND")
                    .withDetail(STR."Transaction item with id \{txItemId} not found")
                    .with("txItemId", txItemId)
                    .build();

            return Either.left(new IdentifiableProblem(txItemId, problem, TRANSACTION_ITEM));
        }

        val txItem= txItemM.orElseThrow();
        txItem.setRejection(rejectionM.orElse(null));

        val savedTxItem = transactionItemRepository.save(txItem);

        return Either.right(savedTxItem.getTransaction());
    }

    public Optional<TransactionEntity> findById(String transactionId) {

        return transactionRepository.findById(transactionId);
    }

    public List<TransactionEntity> findByAllId(Set<String> transactionIds) {
        return transactionRepository.findAllById(transactionIds);
    }

    public List<TransactionEntity> findAllByStatus(String organisationId,
                                                   List<ValidationStatus> validationStatuses,
                                                   List<TransactionType> transactionType) {
        return transactionRepository.findAllByStatus(organisationId, validationStatuses, transactionType);
    }

    public List<TransactionEntity> listAll() {
        return transactionRepository.findAll();
    }

    private static Either<IdentifiableProblem, TransactionEntity> transactionFailedResponse(String transactionId) {
        val problem = Problem.builder()
                .withTitle("CANNOT_APPROVE_FAILED_TX")
                .withDetail(STR."Cannot approve a failed transaction, transactionId: \{transactionId}")
                .with("transactionId", transactionId)
                .build();

        return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
    }

    private static Either<IdentifiableProblem, TransactionEntity> transactionRejectedResponse(String transactionId) {
        val problem = Problem.builder()
                .withTitle("CANNOT_APPROVE_REJECTED_TX")
                .withDetail(STR."Cannot approve a rejected transaction, transactionId: \{transactionId}")
                .withStatus(METHOD_NOT_ALLOWED)
                .with("transactionId", transactionId)
                .build();

        return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
    }

    private static Either<IdentifiableProblem, TransactionEntity> txNotFoundResponse(String txId) {
        val problem = Problem.builder()
                .withTitle("TX_NOT_FOUND")
                .withDetail(STR."Transaction with id \{txId} not found")
                .with("txId", txId)
                .build();

        return Either.left(new IdentifiableProblem(txId, problem, TRANSACTION));
    }

    private static ThrowableProblem createDBError(String transactionId, DataAccessException dae) {
        val problem = Problem.builder()
                .withTitle("DB_ERROR")
                .withDetail(STR."DB error approving transaction publish:\{transactionId}")
                .with("transactionId", transactionId)
                .with("error", dae.getMessage())
                .build();
        return problem;
    }

}

//    public Either<Problem, Boolean> changeTransactionComment(String txId, String userComment) {
//        val txM = transactionRepository.findById(txId);
//
//        if (txM.isEmpty()) {
//            return Either.left(Problem.builder()
//                    .withTitle("TX_NOT_FOUND")
//                    .withDetail(STR."Transaction with id \{txId} not found")
//                    .with("txId", txId)
//                    .build()
//            );
//        }
//
//        val tx = txM.orElseThrow();
//
//        tx.setUserComment(userComment);
//
//        val savedTx = transactionRepository.save(tx);
//
//        return Either.right(savedTx.getUserComment().equals(userComment));
//    }