package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.zalando.problem.Problem;

import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;

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
