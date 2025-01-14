package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
@Getter
@Setter
@AllArgsConstructor
public class FilteringParametersView {

    private List<TransactionType> transactionTypes = List.of();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate to;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate accountingPeriodFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate accountingPeriodTo;

    private List<String> transactionNumbers = List.of();

}
