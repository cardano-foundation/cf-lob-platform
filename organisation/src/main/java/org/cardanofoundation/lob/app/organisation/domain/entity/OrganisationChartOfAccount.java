package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "organisation_chart_of_account")
public class OrganisationChartOfAccount {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "customerCode", column = @Column(name = "customer_code"))
    })
    private Id id;

    @Column(name = "ref_code", nullable = false)
    private String refCode;

    @Column(name = "event_ref_code", nullable = false)
    private String eventRefCode;

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
