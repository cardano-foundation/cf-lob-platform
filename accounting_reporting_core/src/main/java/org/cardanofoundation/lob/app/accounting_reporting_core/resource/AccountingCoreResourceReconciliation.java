package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationRejectionCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service.AccountingCorePresentationViewService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationRejectionCodeRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReconcileResponseView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReconciliationResponseView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountingCoreResourceReconciliation {

    private final AccountingCorePresentationViewService accountingCorePresentationService;
    private final AccountingCoreService accountingCoreService;

    @Tag(name = "Reconciliation", description = "Reconciliation API")
    @Operation(description = "Start the Reconciliation", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReconcileResponseView.class))}
            )
    })
    @PostMapping(value = "/reconcile/trigger", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reconcileTriggerAction(@Valid @RequestBody ReconciliationRequest body) {
        return accountingCoreService.scheduleReconcilation(body.getOrganisationId(), body.getDateFrom(), body.getDateTo()).fold(problem -> {
            return ResponseEntity.status(problem.getStatus().getStatusCode()).body(ReconcileResponseView.createFail(problem.getTitle(), body.getDateFrom(), body.getDateTo(), problem));
        }, success -> {
            return ResponseEntity.ok(ReconcileResponseView.createSuccess("We have received your reconcile request now.", body.getDateFrom(), body.getDateTo()));
        });
    }

    @Operation(description = "Get the Reconciliations", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReconciliationResponseView.class))}
            )
    })
    @Tag(name = "Reconciliation", description = "Reconciliation API")
    @PostMapping(value = "/transactions-reconcile", produces = "application/json")
    public ResponseEntity<?> reconcileStart(@Valid @RequestBody ReconciliationFilterRequest body,
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        body.setLimit(limit);
        body.setPage(page);

        val reconciliationResponseView = accountingCorePresentationService.allReconciliationTransaction(body);

        return ResponseEntity.ok().body(reconciliationResponseView);
    }

    @Operation(description = "Reconciliation Rejection Codes", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReconciliationRejectionCodeRequest.class))}
            )
    })
    @Tag(name = "Reconciliation", description = "Reconciliation API")
    @GetMapping(value = "/transactions-rejection-codes", produces = "application/json")
    public ResponseEntity<?> reconciliationRejectionCode() {
        return ResponseEntity.ok().body(ReconciliationRejectionCodeRequest.values());
    }

}
