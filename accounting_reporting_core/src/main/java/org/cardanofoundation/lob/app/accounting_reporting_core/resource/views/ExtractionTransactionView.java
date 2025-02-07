package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExtractionTransactionView {

    private long total;

    private List<ExtractionTransactionItemView> transactions;

}
