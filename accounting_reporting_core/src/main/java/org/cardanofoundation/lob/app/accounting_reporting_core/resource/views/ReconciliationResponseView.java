package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
public class ReconciliationResponseView {
    private Long total = 0L;
    private TransactionReconciliationStatisticView statistic;

    @JsonProperty("transactions")
    private Set<TransactionReconciliationTransactionsView> transactions = new HashSet<>();
}
