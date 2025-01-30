package org.cardanofoundation.lob.app.support.spring_audit;

import static jakarta.persistence.TemporalType.TIMESTAMP;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import org.javers.core.metamodel.annotation.DiffIgnore;

@Setter
@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class CommonDateOnlyLockableEntity {

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "locked_at")
    @DiffIgnore
    protected LocalDateTime lockedAt;

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

    public Optional<LocalDateTime> getLockedAt() {
        return Optional.ofNullable(lockedAt);
    }


}
