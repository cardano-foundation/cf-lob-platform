package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class FilteringParametersView {

    private List<TransactionType> transactionTypes = List.of();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate to;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private YearMonth accountingPeriodFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private YearMonth accountingPeriodTo;

    private List<String> transactionNumbers = List.of();
}
