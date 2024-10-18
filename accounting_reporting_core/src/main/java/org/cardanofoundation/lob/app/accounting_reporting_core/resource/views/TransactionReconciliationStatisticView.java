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

    private Long OK = 0L;

    private Long NOK = 0L;

    private Long NEVER = 0L;

    private Long TOTAL = 0L;



}
