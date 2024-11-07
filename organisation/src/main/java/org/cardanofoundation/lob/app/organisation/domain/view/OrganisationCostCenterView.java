package org.cardanofoundation.lob.app.organisation.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrganisationCostCenterView {

    private String customerCode;

    private String externalCustomerCode;

    private String name;
}
