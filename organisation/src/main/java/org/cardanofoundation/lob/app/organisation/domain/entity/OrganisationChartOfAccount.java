package org.cardanofoundation.lob.app.organisation.domain.entity;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.envers.Audited;

import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "organisation_chart_of_account")
@Audited
@EntityListeners({ AuditingEntityListener.class })
public class OrganisationChartOfAccount extends CommonEntity implements Persistable<OrganisationChartOfAccount.Id> {

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
