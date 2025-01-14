package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchSearchRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    @NotBlank
    private String organisationId;

    @ArraySchema(arraySchema = @Schema(example = "[\"APPROVE\", \"PENDING\", \"INVALID\", \"PUBLISH\", \"PUBLISHED\"]", implementation = LedgerDispatchStatusView.class))
    private Set<LedgerDispatchStatusView> batchStatistics = Set.of();

    @ArraySchema(arraySchema = @Schema(example = "[\"OK\",\"NOK\"]", implementation = TransactionStatus.class))
    private Set<TransactionStatus> txStatus = Set.of();

    @ArraySchema(arraySchema = @Schema(example = "[\"VendorPayment\",\"BillCredit\"]", implementation = TransactionType.class))
    private Set<TransactionType> transactionTypes = Set.of();

    @Schema(example = "2014-01-01")
    @Nullable
    private LocalDate from;

    @Schema(example = "2024-12-31")
    @Nullable
    private LocalDate To;

    @JsonIgnore
    private Integer limit;

    @JsonIgnore
    private Integer page;

}
