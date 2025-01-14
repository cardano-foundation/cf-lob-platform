package org.cardanofoundation.lob.app.organisation.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;
import org.hibernate.envers.Audited;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "organisation_project")
@Builder
@Audited
@EntityListeners({ AuditingEntityListener.class })
public class OrganisationProject extends CommonEntity implements Persistable<OrganisationProject.Id> {

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
