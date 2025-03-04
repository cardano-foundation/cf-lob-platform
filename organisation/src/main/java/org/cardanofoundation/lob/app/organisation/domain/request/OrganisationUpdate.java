package org.cardanofoundation.lob.app.organisation.domain.request;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationUpdate {

    @Nullable
    @Schema(example = "My company name")
    private String name;

    @Nullable
    @Schema(example = "Ballyhealy")
    private String city;

    @Nullable
    @Schema(example = "Y35 C6KC")
    private String postCode;

    @Nullable
    @Schema(example = "Co. Wexford")
    private String province;

    @Nullable
    @Schema(example = "Ballyhealy Cottage")
    private String address;

    @Nullable
    @Schema(example = "0035863286566")
    private String phoneNumber;

    @Nullable
    @Schema(example = "lob@cardanofoundation.org")
    private String adminEmail;

    @Nullable
    @Schema(example = "http://cardanofoundation.org")
    private String websiteUrl;

    @Nullable
    @Schema(example = "ISO_4217:CHF")
    private String currencyId;

    @Nullable
    @Schema(example = "ISO_4217:CHF")
    private String reportCurrencyId;

}
