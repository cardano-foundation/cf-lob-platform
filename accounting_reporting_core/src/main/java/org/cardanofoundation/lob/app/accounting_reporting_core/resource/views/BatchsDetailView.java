package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@AllArgsConstructor
public class BatchsDetailView {

    private Long total;
    private List<BatchView> batchs;

}
