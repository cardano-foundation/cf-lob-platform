package org.cardanofoundation.lob.app.organisation.domain.view;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;



@Getter
@Builder
@AllArgsConstructor
    public class OrganisationChartOfAccountView {

    private String customerCode;

    private String refCode;

    private String eventRefCode;

    private String name;


}
