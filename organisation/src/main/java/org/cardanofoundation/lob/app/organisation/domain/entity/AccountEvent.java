package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "organisation_account_event")
@Builder
@Audited
@EntityListeners({ AuditingEntityListener.class })
@ToString
public class AccountEvent {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "customerCode", column = @Column(name = "customer_code"))
    })
    private OrganisationAwareId id;

    @Column(name = "name", nullable = false)
    private String name;

}
