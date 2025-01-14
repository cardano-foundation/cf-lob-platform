package org.cardanofoundation.lob.app.support.spring_audit;


import static jakarta.persistence.TemporalType.TIMESTAMP;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import org.hibernate.envers.Audited;
import org.javers.core.metamodel.annotation.DiffIgnore;

@Setter
@Getter
@MappedSuperclass
@NoArgsConstructor
@Audited
public abstract class CommonEntity {

    @Column(name = "created_by")
    @CreatedBy
    @DiffIgnore
    protected String createdBy;

    @Column(name = "updated_by")
    @LastModifiedBy
    @DiffIgnore
    protected String updatedBy;

    @Temporal(TIMESTAMP)
    @Column(name = "created_at")
    @CreatedDate
    @DiffIgnore
    protected LocalDateTime createdAt;

    @Temporal(TIMESTAMP)
    @Column(name = "updated_at")
    @LastModifiedDate
    @DiffIgnore
    protected LocalDateTime updatedAt;

    @Transient
    @DiffIgnore
    protected boolean isNew = true;

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public boolean isNew() {
        return isNew;
    }

}
