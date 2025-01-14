package org.cardanofoundation.lob.app.support.spring_audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Setter
@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class CommonDateOnlyEntity {

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
