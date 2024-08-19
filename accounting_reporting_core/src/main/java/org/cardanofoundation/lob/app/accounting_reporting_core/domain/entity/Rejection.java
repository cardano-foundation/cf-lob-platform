package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import lombok.*;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Rejection {

    private RejectionCode rejectionCode;

}
