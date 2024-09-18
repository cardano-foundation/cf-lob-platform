package org.cardanofoundation.lob.app.support.spring_audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Setter
@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class AuditEntity {

    @Column(name = "created_by")
    @CreatedBy
    @DiffIgnore
    protected String createdBy = "system";

    @Column(name = "updated_by")
    @LastModifiedBy
    @DiffIgnore
    protected String updatedBy = "system";

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

    @PrePersist
    protected void onCreate() {
        val now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        this.isNew = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isNew() {
        return isNew;
    }

}
