package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "organisation_cost_center")
@Builder
public class OrganisationCostCenter {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "customerCode", column = @Column(name = "customer_code"))
    })
    private Id id;

    @Column(name = "external_customer_code", nullable = false)
    private String externalCustomerCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class Id {

        private String organisationId;
        private String customerCode;

    }

}
