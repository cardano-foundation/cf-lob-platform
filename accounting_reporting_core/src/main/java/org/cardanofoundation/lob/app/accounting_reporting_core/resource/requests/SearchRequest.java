package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    @NotBlank
    private String organisationId;

    @ArraySchema(arraySchema = @Schema(example = "[\"FAILED\",\"VALIDATED\"]", implementation = ValidationStatus.class))
    private List<ValidationStatus> status = List.of();

    @ArraySchema(arraySchema = @Schema(example = "[\"CardRefund\",\"Journal\",\"ExpenseReport\"]", implementation = TransactionType.class))
    private List<TransactionType> transactionType = List.of();

}
