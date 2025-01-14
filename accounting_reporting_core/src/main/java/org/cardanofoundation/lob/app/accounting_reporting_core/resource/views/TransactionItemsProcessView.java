package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zalando.problem.Problem;

@Getter
@Setter
@AllArgsConstructor
public class TransactionItemsProcessView {

    private String transactionItemId;
    private Boolean success;
    private Optional<Problem> error;

    public static TransactionItemsProcessView createSuccess(String transactionItemId) {
        return new TransactionItemsProcessView(
                transactionItemId,
                true,
                Optional.empty()
        );
    }

    public static TransactionItemsProcessView createFail(String transactionItemId,
                                                         Problem error) {
        return new TransactionItemsProcessView(transactionItemId, false, Optional.of(error));
    }

}
