package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "organisation_vat")
public class OrganisationVat extends AuditEntity {

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
