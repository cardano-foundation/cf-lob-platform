package org.cardanofoundation.lob.app.organisation.domain.entity;

import static jakarta.persistence.FetchType.LAZY;
import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.envers.Audited;

import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "organisation_chart_of_account_type")
@Audited
@EntityListeners({AuditingEntityListener.class})
public class OrganisationChartOfAccountType extends CommonEntity {

    @Id
    private String  id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", referencedColumnName = "organisation_id")
    private Organisation organisation;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "type", orphanRemoval = true, fetch = LAZY, cascade = CascadeType.ALL)
    private Set<OrganisationChartOfAccountSubType> subType = new LinkedHashSet<>();

    public static String id(Organisation organisationId,
                            String name) {
        return digestAsHex(STR."\{organisationId.getId()}::\{name}");
    }

}
