package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zalando.problem.Problem;


@Getter
@Setter
@AllArgsConstructor
public class ReportResponseView {

    private boolean success;

    private Set<ReportView> report;
    private Optional<Problem> error;

    public static ReportResponseView createSuccess(Set<ReportView> reportView) {
        return new ReportResponseView(
                true,
                reportView,
                Optional.empty()
        );
    }

    public static ReportResponseView createFail(Problem error) {
        return new ReportResponseView(false, new HashSet<>(), Optional.of(error));
    }
}
