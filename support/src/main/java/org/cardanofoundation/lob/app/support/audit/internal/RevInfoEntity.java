package org.cardanofoundation.lob.app.support.audit.internal;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

@Entity
@RevisionEntity
@Table(name = "revinfo")
public class RevInfoEntity {

    @Id
    @GeneratedValue(generator = "revinfo_gen")
    @SequenceGenerator(name = "revinfo_gen", sequenceName = "revinfo_seq", initialValue = 1, allocationSize = 1)
    @RevisionNumber
    @Column(name = "rev")
    private Long rev;

    @RevisionTimestamp
    @Column(name = "rev_timestamp")
    private LocalDateTime revTimestamp;

}
