package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;

import java.math.BigDecimal;
import java.util.Optional;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransactionItem {

    @LOB_ERPSourceVersionRelevant
    @NotBlank private String id;

    @LOB_ERPSourceVersionRelevant
    @NotNull private BigDecimal amountFcy;

    @LOB_ERPSourceVersionRelevant
    @NotNull private BigDecimal amountLcy;

    @Builder.Default
    @LOB_ERPSourceVersionRelevant
    private Optional<Account> accountDebit = Optional.empty();

    @Builder.Default
    @LOB_ERPSourceVersionRelevant
    private Optional<Account> accountCredit = Optional.empty();

    @Builder.Default
    private Optional<AccountEvent> accountEvent = Optional.empty();

    @Builder.Default
    @LOB_ERPSourceVersionRelevant
    private Optional<Project> project = Optional.empty();

    @Builder.Default
    @LOB_ERPSourceVersionRelevant
    private Optional<CostCenter> costCenter = Optional.empty();

    @Builder.Default
    @LOB_ERPSourceVersionRelevant
    private Optional<Document> document = Optional.empty(); // initially we allow empty but later as part of business rules we check if document is present

    @NotNull
    @PositiveOrZero
    @LOB_ERPSourceVersionRelevant
    private BigDecimal fxRate;

    public static String id(String transactionId,
                            String lineNo) {
        return digestAsHex(STR."\{transactionId}::\{lineNo}");
    }

}
