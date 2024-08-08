package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.zalando.problem.Problem;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class TransactionItemsProcessView {

    private String transactionId;
    private String transactionItemId;
    private Boolean success;
    private Optional<Problem> error;

    public static TransactionItemsProcessView createSuccess(String transactionId,
                                                            String transactionItemId) {
        return new TransactionItemsProcessView(
                transactionId,
                transactionItemId,
                true,
                Optional.empty()
        );
    }

    public static TransactionItemsProcessView createFail(String transactionId,
                                                         String transactionItemId,
                                                         Problem error) {
        return new TransactionItemsProcessView(transactionId, transactionItemId, false, Optional.of(error));
    }

}
