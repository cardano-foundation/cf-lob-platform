package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
//@AllArgsConstructor
public class BatchsDetailView {
    private Long total;
    private List<BatchView> batchs;
}
