package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zalando.problem.Problem;

@Getter
@Setter
@AllArgsConstructor
public class BatchReprocessView {

    private String batchId;

    private boolean success;

    private Optional<Problem> error;

    public static BatchReprocessView createSuccess(String batchId) {
        return new BatchReprocessView(
                batchId,
                true,
                Optional.empty()
        );
    }

    public static BatchReprocessView createFail(String batchId,
                                                Problem error) {
        return new BatchReprocessView(batchId, false, Optional.of(error));
    }

}
