package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Either;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;
import org.cardanofoundation.lob.app.support.problem_support.IdentifiableProblem;
import org.zalando.problem.Problem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class TransactionItemsProcessRejectView {

    private String transactionId;

    private boolean success;

    private Optional<LedgerDispatchStatusView> statistic;

    @JsonProperty("items")
    private Set<TransactionItemsProcessView> transactionItemsProcessViewSet = new LinkedHashSet<>();

    private Optional<Problem> error;

    public static TransactionItemsProcessRejectView createSuccess(String transactionId, LedgerDispatchStatusView statistic, Set<TransactionItemsProcessView> items) {
        return new TransactionItemsProcessRejectView(
                transactionId,
                true,
                Optional.of(statistic),
                items,
                Optional.empty()
        );
    }

    public static TransactionItemsProcessRejectView createFail(String transactionId,
                                                               Problem error) {
        return new TransactionItemsProcessRejectView(transactionId, false, null, Set.of(), Optional.of(error));
    }

}
