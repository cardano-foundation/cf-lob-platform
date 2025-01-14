package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;
import org.zalando.problem.Problem;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

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
