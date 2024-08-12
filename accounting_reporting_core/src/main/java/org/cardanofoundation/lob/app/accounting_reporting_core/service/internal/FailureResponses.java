package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.TransactionItemsRejectionRequest;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.springframework.dao.DataAccessException;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import java.util.List;

import static org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem.IdType.TRANSACTION;
import static org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem.IdType.TRANSACTION_ITEM;
import static org.zalando.problem.Status.METHOD_NOT_ALLOWED;

public final class FailureResponses {

    public static Either<IdentifiableProblem, TransactionEntity> transactionFailedResponse(String transactionId) {
        val problem = Problem.builder()
                .withTitle("CANNOT_APPROVE_FAILED_TX")
                .withDetail(STR."Cannot approve a failed transaction, transactionId: \{transactionId}")
                .with("transactionId", transactionId)
                .build();

        return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
    }

    public static Either<IdentifiableProblem, TransactionEntity> transactionRejectedResponse(String transactionId) {
        val problem = Problem.builder()
                .withTitle("CANNOT_APPROVE_REJECTED_TX")
                .withDetail(STR."Cannot approve a rejected transaction, transactionId: \{transactionId}")
                .withStatus(METHOD_NOT_ALLOWED)
                .with("transactionId", transactionId)
                .build();

        return Either.left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
    }

    public static Either<IdentifiableProblem, TransactionEntity> transactionNotFoundResponse(String txId) {
        val problem = Problem.builder()
                .withTitle("TX_NOT_FOUND")
                .withDetail(STR."Transaction with id \{txId} not found")
                .with("txId", txId)
                .build();

        return Either.left(new IdentifiableProblem(txId, problem, TRANSACTION));
    }

    public static ThrowableProblem createTransactionDBError(String transactionId, DataAccessException dae) {
        val problem = Problem.builder()
                .withTitle("DB_ERROR")
                .withDetail(STR."DB error approving transaction publish:\{transactionId}")
                .with("transactionId", transactionId)
                .with("error", dae.getMessage())
                .build();
        return problem;
    }

    public static List<Either<IdentifiableProblem, TransactionItemEntity>> transactionNotFoundResponse(TransactionItemsRejectionRequest transactionItemsRejectionRequest,
                                                                                                        String transactionId) {
        return transactionItemsRejectionRequest.getTransactionItemsRejections()
                .stream()
                .map(txItemRejectionRequest -> {
                    val problem = Problem.builder()
                            .withTitle("TX_NOT_FOUND")
                            .withDetail(STR."Transaction with id \{transactionId} not found")
                            .with("transactionId", transactionId)
                            .build();

                    return Either.<IdentifiableProblem, TransactionItemEntity>left(new IdentifiableProblem(transactionId, problem, TRANSACTION));
                }).toList();
    }

    public static Either<IdentifiableProblem, TransactionItemEntity> transactionItemCannotRejectAlreadyApprovedResponse(String transactionId,
                                                                                                                        String txItemId) {
        val problem = Problem.builder()
                .withTitle("TX_ALREADY_APPROVED_CANNOT_REJECT_TX_ITEM")
                .withDetail(STR."Cannot reject transaction item \{txItemId} because transaction \{transactionId} has already been approved for dispatch")
                .with("transactionId", transactionId)
                .with("transactionItemId", txItemId)
                .build();

        return Either.left(new IdentifiableProblem(txItemId, problem, TRANSACTION_ITEM));
    }

    public static Either<IdentifiableProblem, TransactionItemEntity> transactionItemNotFoundResponse(String transactionId,
                                                                                                     String txItemId) {
        val problem = Problem.builder()
                .withTitle("TX_ITEM_NOT_FOUND")
                .withDetail(STR."Transaction item with id \{txItemId} not found")
                .with("transactionId", transactionId)
                .with("transactionItemId", txItemId)
                .build();

        return Either.left(new IdentifiableProblem(txItemId, problem, TRANSACTION_ITEM));
    }

}
