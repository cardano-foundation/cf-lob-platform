package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zalando.problem.Problem;

@Getter
@Setter
@AllArgsConstructor
public class ExtractionTransactionView {
    private boolean success;

    private long total;

    private List<ExtractionTransactionItemView> transactions;

    private Optional<Problem> error;

    public static ExtractionTransactionView createSuccess(List<ExtractionTransactionItemView> transactions) {
        return new ExtractionTransactionView(
                true,
                transactions.stream().count(),
                transactions,
                Optional.empty()
        );
    }

    public static ExtractionTransactionView createFail(Problem error) {
        return new ExtractionTransactionView(false, 0L, List.of(), Optional.of(error));
    }
}
