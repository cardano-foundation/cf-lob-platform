package org.cardanofoundation.lob.app.organisation.domain.request;

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

    @Schema(example = "My company name")
    private String name;

    @Schema(example = "Ballyhealy")
    private String city;

    @Schema(example = "Y35 C6KC")
    private String postCode;

    @Schema(example = "Co. Wexford")
    private String province;

    @Schema(example = "Ballyhealy Cottage")
    private String address;

    @Schema(example = "0035863286566")
    private String phoneNumber;

    @Schema(example = "lob@cardanofoundation.org")
    private String adminEmail;

    @Schema(example = "http://cardanofoundation.org")
    private String websiteUrl;

    @Schema(example = "ISO_4217:CHF")
    private String currencyId;

    @Schema(example = "ISO_4217:CHF")
    private String reportCurrencyId;

}
