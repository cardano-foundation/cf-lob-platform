package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;
import org.hibernate.envers.Audited;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "organisation_chart_of_account_sub_type")
@Audited
@EntityListeners({AuditingEntityListener.class})
public class OrganisationChartOfAccountSubType extends CommonEntity {

    @Id
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type", referencedColumnName = "id")
    private OrganisationChartOfAccountType type;

    public static String id(OrganisationChartOfAccountType chartOfAccountType,
                            String name) {
        return digestAsHex(STR."\{chartOfAccountType}::\{name}");
    }

}
