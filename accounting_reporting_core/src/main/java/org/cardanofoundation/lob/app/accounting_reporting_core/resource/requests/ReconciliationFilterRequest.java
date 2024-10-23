package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationRejectionCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
//@Builder todo: For testing
@NoArgsConstructor
@Slf4j
public class ReconciliationFilterRequest {
    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    private String organisationId;

    @Schema(example = "UNRENCONCILED")
    private ReconciliationFilterStatusRequest filter;

    @ArraySchema(arraySchema = @Schema(example = "[\"TX_NOT_IN_LOB\",\"SOURCE_RECONCILATION_FAIL\",\"SINK_RECONCILATION_FAIL\",\"TX_NOT_IN_ERP\"]"))
    private Set<ReconcilationRejectionCode> reconciliationRejectionCode= new  HashSet<>();

    @JsonIgnore
    private Integer limit;

    @JsonIgnore
    private Integer page;

}
