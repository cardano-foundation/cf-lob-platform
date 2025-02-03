package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.metric;

import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounting_core_dashboard")
public class DashboardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organisation_id")
    private String organisationID;
    private String name;
    private String description;

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ChartEntity> charts;

}
