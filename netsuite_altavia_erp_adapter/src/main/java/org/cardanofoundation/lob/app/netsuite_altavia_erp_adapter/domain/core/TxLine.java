package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.validation.constraints.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;

// https://docs.google.com/spreadsheets/d/1iGo1t2bLuWSONOYo6kG9uXSzt7laCrM8gluKkx8tmn0/edit#gid=501685631
@JsonIgnoreProperties(ignoreUnknown = true)
public record TxLine(

        @JsonProperty("Line ID")
        @PositiveOrZero
        Integer lineID,

        @JsonProperty("Subsidiary (no hierarchy)")
        @PositiveOrZero
        Long subsidiary,

        @JsonProperty("Type")
        String type,

        @JsonProperty("Date")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate date,

        @JsonProperty("ID")
        String id,

        @JsonProperty("Company Name")
        String companyName,

        @JsonProperty("Tax Item")
        String taxItem,

        @JsonProperty("Cost Center (no hierarchy)")
        @Nullable
        String costCenter,

        @JsonProperty("Transaction Number")
        @NotBlank
        String transactionNumber,

        @JsonProperty("Document Number")
        @Nullable
        String documentNumber,

        @JsonProperty("Number")
        String number,

        @JsonProperty("Name")
        @Nullable
        String name,

        @JsonProperty("Project (no hierarchy)")
        String project,

        @JsonProperty("Account (Main)")
        String accountMain,

        @JsonProperty("Memo (Main)")
        String memo,

        @JsonProperty("Currency")
        @NotNull
        String currency,

        @JsonProperty("Symbol")
        @NotNull
        String currencySymbol,

        // this is accounting period date
        @JsonProperty("End Date")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate endDate,

        @NotNull
        @JsonProperty("Exchange Rate")
        @Positive
        BigDecimal exchangeRate,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Debit) (Foreign Currency)") BigDecimal amountDebitForeignCurrency,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Credit) (Foreign Currency)") BigDecimal amountCreditForeignCurrency,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Debit)") BigDecimal amountDebit,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Credit)") BigDecimal amountCredit

) { }
