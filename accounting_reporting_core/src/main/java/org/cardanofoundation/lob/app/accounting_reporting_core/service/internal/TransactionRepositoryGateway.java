package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Rejection;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionItemsRejectionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionsRequest;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.FailureResponses.*;
import static org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem.IdType.TRANSACTION;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.zalando.problem.Status.METHOD_NOT_ALLOWED;

@Service
@org.jmolecules.ddd.annotation.Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionRepositoryGateway {

    private final TransactionItemRepository transactionItemRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;

    @Transactional(propagation = REQUIRES_NEW)
    // TODO optimise performance because we have to load transaction from db each time and we don't save it in bulk
    protected Either<IdentifiableProblem, TransactionEntity> approveTransaction(String transactionId) {
        log.info("Approving transaction: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            return transactionNotFoundResponse(transactionId);
        }

        val tx = txM.orElseThrow();

        if (tx.getAutomatedValidationStatus() == FAILED) {
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
            return transactionNotFoundResponse(transactionId);
        }

        val tx = txM.orElseThrow();

        if (tx.getAutomatedValidationStatus() == FAILED) {
            return transactionFailedResponse(transactionId);
        }

        if (tx.hasAnyRejection()) {
            return transactionRejectedResponse(transactionId);
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

    @Transactional
    public List<Either<IdentifiableProblem, TransactionEntity>> approveTransactions(TransactionsRequest transactionsRequest) {
        val organisationId = transactionsRequest.getOrganisationId();

        val transactionIds = transactionsRequest.getTransactionIds();

        val transactionsApprovalResponseListE = new ArrayList<Either<IdentifiableProblem, TransactionEntity>>();
        for (val transactionId : transactionIds) {
            try {
                val transactionEntities = approveTransaction(transactionId.getId());

                transactionsApprovalResponseListE.add(transactionEntities);
            } catch (DataAccessException dae) {
                log.error("Error approving transaction: {}", transactionId, dae);

                val problem = FailureResponses.createTransactionDBError(transactionId.getId(), dae);

                transactionsApprovalResponseListE.add(Either.left(new IdentifiableProblem(transactionId.getId(), problem, TRANSACTION)));
            }
        }

        val transactionSuccesses = transactionsApprovalResponseListE.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .collect(toSet());

        ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, transactionSuccesses);

        return transactionsApprovalResponseListE;
    }

    @Transactional
    public List<Either<IdentifiableProblem, TransactionEntity>> approveTransactionsDispatch(TransactionsRequest transactionsRequest) {
        val organisationId = transactionsRequest.getOrganisationId();

        val transactionIds = transactionsRequest.getTransactionIds();

        val transactionsApprovalResponseListE = new ArrayList<Either<IdentifiableProblem, TransactionEntity>>();
        for (val transactionId : transactionIds) {
            try {
                val transactionEntities = approveTransactionsDispatch(transactionId.getId());

                transactionsApprovalResponseListE.add(transactionEntities);
            } catch (DataAccessException dae) {
                log.error("Error approving transaction publish / dispatch: {}", transactionId, dae);

                val problem = createTransactionDBError(transactionId.getId(), dae);

                transactionsApprovalResponseListE.add(Either.left(new IdentifiableProblem(transactionId.getId(), problem, TRANSACTION)));
            }
        }

        val transactionSuccesses = transactionsApprovalResponseListE.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .collect(toSet());

        ledgerService.checkIfThereAreTransactionsToDispatch(organisationId, transactionSuccesses);

        return transactionsApprovalResponseListE;
    }

    @Transactional
    public List<Either<IdentifiableProblem, TransactionItemEntity>> rejectTransactionItems(TransactionItemsRejectionRequest transactionItemsRejectionRequest) {
        log.info("Rejecting transaction items: {}", transactionItemsRejectionRequest);

        val organisationId = transactionItemsRejectionRequest.getOrganisationId();
        val transactionId = transactionItemsRejectionRequest.getTransactionId();

        val txM = transactionRepository.findById(transactionId);

        val transactionItemEntitiesE = new ArrayList<Either<IdentifiableProblem, TransactionItemEntity>>();

        if (txM.isEmpty()) {
            return transactionNotFoundResponse(transactionItemsRejectionRequest, transactionId);
        }

        val tx = txM.orElseThrow();

        for (val txItemRejection : transactionItemsRejectionRequest.getTransactionItemsRejections()) {
            val txItemId = txItemRejection.getTxItemId();
            val rejectionCode = txItemRejection.getRejectionCode();

            val txItemM = transactionItemRepository.findById(txItemId);

            if (txItemM.isEmpty()) {
                transactionItemEntitiesE.add(transactionItemNotFoundResponse(transactionId, txItemId));
                continue;
            }

            val txItem = txItemM.orElseThrow();
            if (tx.getLedgerDispatchApproved()) {
                transactionItemEntitiesE.add(transactionItemCannotRejectAlreadyApprovedForDispatchResponse(transactionId, txItemId));
                continue;
            }

            txItem.setRejection(Optional.of(new Rejection(rejectionCode)));

            val savedTxItem = transactionItemRepository.save(txItem);

            transactionItemEntitiesE.add(Either.right(savedTxItem));
        }

        return transactionItemEntitiesE;
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