package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service.AccountingCorePresentationViewService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionReconciliationStatisticView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionReconciliationView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountingCoreResourceReconciliation {
    private final AccountingCorePresentationViewService accountingCorePresentationService;
    private final AccountingCoreService accountingCoreService;

    @Tag(name = "Reconciliation", description = "Reconciliation API")
    @Operation(description = "Transaction list", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReconciliationRequest.class))}
            )
    })
    @PostMapping(value = "/reconciliation/trigger", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reconcileTriggerAction(@Valid @RequestBody ReconciliationRequest body) {

        return accountingCoreService.scheduleReconcilation(body.getOrganisationId(), body.getDateFrom(), body.getDateTo()).fold(problem -> {
            return ResponseEntity.status(problem.getStatus().getStatusCode()).body(problem);
        }, success -> {
            return ResponseEntity.ok().build();
        });

    }

    @Operation(description = "Transaction list", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionReconciliationStatisticView.class))}
            )
    })
    @Tag(name = "Reconciliation", description = "Reconciliation API")
    @RequestMapping(value = "/reconciliation", method = POST, produces = "application/json")
    public ResponseEntity<?> reconcileStart() {
        TransactionReconciliationStatisticView transactions = accountingCorePresentationService.allReconciliationTransaction();


        return ResponseEntity.ok().body(transactions);
    }
}
