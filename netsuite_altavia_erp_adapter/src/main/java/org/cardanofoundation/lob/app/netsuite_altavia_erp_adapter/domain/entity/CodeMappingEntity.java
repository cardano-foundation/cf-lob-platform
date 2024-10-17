package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity(name = "netsuite.CodeMappingEntity")
@Table(name = "netsuite_adapter_code_mapping")
@NoArgsConstructor
public class CodeMappingEntity extends CommonEntity implements Persistable<CodeMappingEntity.Id> {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "mappingId", column = @Column(name = "mapping_id")),
            @AttributeOverride(name = "internalId", column = @Column(name = "internal_id")),
            @AttributeOverride(name = "type", column = @Column(name = "code_type"))
    })
    private Id id;

    @Column(name = "customerCode")
    private String code;

    @Embeddable
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {

        private String mappingId;

        private Long internalId;

        @Enumerated(STRING)
        private CodeMappingType type;

    }

}
