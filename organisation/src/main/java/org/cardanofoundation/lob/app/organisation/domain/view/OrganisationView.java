package org.cardanofoundation.lob.app.organisation.domain.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@AllArgsConstructor
public class OrganisationView {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    String id;

    @Schema(example = "Cardano Foundation")
    String name;

    @Schema(example = "Description")
    String description;

    @Schema(example = "Currency Id")
    String currencyId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(example = "2021-02-05")
    LocalDate accountPeriodFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(example = "2024-02-05")
    LocalDate accountPeriodTo;

    @Schema(example = "lob@cardanofoundation.org")
    private String adminEmail;

}
