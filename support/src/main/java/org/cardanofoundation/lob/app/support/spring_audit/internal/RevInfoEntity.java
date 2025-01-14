package org.cardanofoundation.lob.app.support.spring_audit.internal;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "revinfo")
@RevisionEntity
public class RevInfoEntity {

    @Id
    @GeneratedValue(generator = "revinfo_gen")
    @SequenceGenerator(name = "revinfo_gen", sequenceName = "revinfo_seq", initialValue = 1, allocationSize = 1)
    @RevisionNumber
    @Column(name = "rev")
    @Getter
    private Long rev;

    @RevisionTimestamp
    @Column(name = "rev_timestamp")
    @Getter
    private LocalDateTime revTimestamp;

}
