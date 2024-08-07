package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionApprove {
    //48335c2b63cffcef2a3cd0678b65c4fb16420f51110033024209957fbd58ec4e
    @Schema(example = "7e9e8bcbb38a283b41eab57add98278561ab51d23a16f3e3baf3daa461b84ab4")
    private String id;
}
