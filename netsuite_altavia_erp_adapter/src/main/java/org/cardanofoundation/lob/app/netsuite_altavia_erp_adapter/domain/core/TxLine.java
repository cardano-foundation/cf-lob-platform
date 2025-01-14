package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.Dates.ISO_8601_FORMAT_QUASI;

// https://docs.google.com/spreadsheets/d/1iGo1t2bLuWSONOYo6kG9uXSzt7laCrM8gluKkx8tmn0/edit#gid=501685631
@JsonIgnoreProperties(ignoreUnknown = true)
public record TxLine(

        @JsonProperty("line")
        @PositiveOrZero
        @NotNull
        Integer lineID,

        @JsonProperty("subsidiarynohierarchy")
        @PositiveOrZero
        @NotNull
        Long subsidiary,

        @JsonProperty("type")
        @NotNull
        String type,

        @JsonProperty("trandate")
        @JsonFormat(shape = STRING, pattern = ISO_8601_FORMAT_QUASI)
        @NotNull
        LocalDate date,

        @JsonProperty("vendor.entityid")
        @Nullable
        String counterPartyId,

        @JsonProperty("vendor.companyname")
        String counterPartyName,

        @JsonProperty("taxcode")
        String taxItem,

        @Nullable
        @JsonProperty("departmentnohierarchy")
        String costCenter,

        @JsonProperty("transactionnumber")
        @NotBlank
        String transactionNumber,

        @JsonProperty("tranid")
        @Nullable
        String documentNumber,

        @JsonProperty("account.number")
        String number,

        @JsonProperty("account.name")
        @Nullable
        String name,

        @JsonProperty("classnohierarchy")
        String project,

        @JsonProperty("accountmain")
        String accountMain,

        @JsonProperty("currency")
        @NotNull
        String currency,

        @JsonProperty("Currency.symbol")
        @NotNull
        String currencySymbol,

        // this is accounting period date
        @JsonProperty("enddate")
        @JsonFormat(shape = STRING, pattern = ISO_8601_FORMAT_QUASI)
        LocalDate endDate,

        @JsonProperty("datecreated")
        @JsonFormat(shape = STRING, pattern = ISO_8601_FORMAT_QUASI)
        @NotNull
        LocalDateTime dateCreated,

        @JsonProperty("lastmodifieddate")
        @JsonFormat(shape = STRING, pattern = ISO_8601_FORMAT_QUASI)
        @NotNull
        LocalDateTime lastModifiedDate,

        @NotNull
        @JsonProperty("exchangerate")
        @Positive
        BigDecimal exchangeRate,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("debitfxamount") BigDecimal amountDebitForeignCurrency,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("creditfxamount") BigDecimal amountCreditForeignCurrency,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("debitamount") BigDecimal amountDebit,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("creditamount") BigDecimal amountCredit

) {

}
