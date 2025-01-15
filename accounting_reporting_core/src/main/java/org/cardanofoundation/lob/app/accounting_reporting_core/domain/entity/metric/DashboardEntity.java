package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Validable;
import org.springframework.data.domain.Persistable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dashboard")
public class DashboardEntity{

    @Id
    @GeneratedValue
    private int id;

    private String name;
    private String description;
    private String userID;
}
