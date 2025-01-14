package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

@Getter
@Setter
@AllArgsConstructor
//@Builder todo: For testing
@NoArgsConstructor
@Slf4j
public class ExtractionRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    private String organisationId;

    //@Builder.Default todo: For testing
    @Schema(example = "2013-01-02")
    private String dateFrom = "";

    @Schema(example = "2024-05-01")
    private String dateTo = "";

    @ArraySchema(arraySchema = @Schema(example = "[\"FxRevaluation\",\"Journal\",\"CustomerPayment\"] ", implementation = TransactionType.class))
    private List<TransactionType> transactionType = List.of();

    @ArraySchema(arraySchema = @Schema(example = "[\"CARDCH565\",\"CARDHY777\",\"CARDCHRG159\",\"VENDBIL119\"] "))
    private List<String> transactionNumbers = List.of();

}
