package org.cardanofoundation.lob.app.organisation.domain.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

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
@Table(name = "organisation_vat")
@Audited
@EntityListeners({ AuditingEntityListener.class })
public class OrganisationVat extends CommonEntity implements Persistable<OrganisationVat.Id> {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "customerCode", column = @Column(name = "customer_code"))
    })
    private Id id;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

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
