package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.RejectionCode;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionItemsRejectionRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    @NotBlank
    private String organisationId;

    @Schema(example = "38e7e04304c86c1156128f7bdc548d51f175d5bdf83df1b3edda1832cac385dd")
    @NotBlank
    private String transactionId;

    @ArraySchema(arraySchema = @Schema(example = "[ " +
            "{\"txItemId\": \"7e9e8bcbb38a283b41eab57add98278561ab51d23a16f3e3baf3daa461b84ab4\",\"rejectionCode\":\"INCORRECT_VAT_CODE\"}," +
            "{\"txItemId\": \"7bce71783ff8e6501b33ce9797097f5633c069f17e4731d96467cdb311693fcb\",\"rejectionCode\":\"INCORRECT_AMOUNT\"}" +
            "]"))
    @NotNull
    @Size(min = 1)
    @Valid
    private Set<TxItemRejectionRequest> transactionItemsRejections;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class TxItemRejectionRequest {

        @Schema(example = "e7a70ff49619cea765db827cc3a7e6a320d8e0859f7818ed43b6c4bc8025fc60")
        @NotBlank
        private String txItemId;

        @Schema(example = "INCORRECT_VAT_CODE")
        @NotNull
        private RejectionCode rejectionCode;

    }

}
