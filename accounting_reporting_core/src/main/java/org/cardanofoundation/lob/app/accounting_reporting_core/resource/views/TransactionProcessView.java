package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zalando.problem.Problem;

@Getter
@Setter
@AllArgsConstructor
public class TransactionProcessView {

    private String id;
    private Boolean success;
    private Optional<Problem> error;

    public static TransactionProcessView createSuccess(String id) {
        return new TransactionProcessView(id, true, Optional.empty());
    }

    public static TransactionProcessView createFail(String id, Problem error) {
        return new TransactionProcessView(id, false, Optional.of(error));
    }

}
