package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class TransactionReconciliationStatisticView {

    private Long ok = 0L;

    private Long nok = 0L;

    private Long none = 0L;

    private Long total = 0L;
    @JsonProperty("transactions")
    private Set<TransactionReconciliationView> transactionReconciliationViewList = new HashSet<>();
}
