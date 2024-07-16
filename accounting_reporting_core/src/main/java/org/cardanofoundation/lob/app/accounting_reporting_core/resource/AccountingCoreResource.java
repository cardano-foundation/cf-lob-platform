package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.model.AccountingCorePresentationViewService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.model.AccountingCoreResourceService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ExtractionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.BatchView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.BatchsDetailView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountingCoreResource {

    private final AccountingCorePresentationViewService accountingCorePresentationService;
    private final AccountingCoreResourceService accountingCoreResourceService;

    @Tag(name = "Transactions", description = "Transactions API")
    @Operation(description = "Transaction list", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TransactionView.class)))}
            )
    })
    @PostMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listAllAction(@Valid @RequestBody SearchRequest body) {
        List<TransactionView> transactions = accountingCorePresentationService.allTransactions(body);

        return ResponseEntity.ok().body(transactions);
    }

    @Tag(name = "Transactions", description = "Transactions API")
    @Operation(description = "Transaction detail", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionView.class))}
            )
    })
    @GetMapping(value = "/transactions/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transactionDetailSpecific(@Valid @PathVariable("transactionId") @Parameter(example = "7e9e8bcbb38a283b41eab57add98278561ab51d23a16f3e3baf3daa461b84ab4") String transactionId) {

        val transactionEntity = accountingCorePresentationService.transactionDetailSpecific(transactionId);
        if (transactionEntity.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id: {\{transactionId}} could not be found")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        return ResponseEntity.ok().body(transactionEntity);
    }

    @Tag(name = "Transactions", description = "Transactions API")
    @Operation(description = "Transaction types", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "[{\"id\":\"CardCharge\",\"title\":\"Card Charge\"},{\"id\":\"VendorBill\",\"title\":\"Vendor Bill\"},{\"id\":\"CardRefund\",\"title\":\"Card Refund\"},{\"id\":\"Journal\",\"title\":\"Journal\"},{\"id\":\"FxRevaluation\",\"title\":\"Fx Revaluation\"},{\"id\":\"Transfer\",\"title\":\"Transfer\"},{\"id\":\"CustomerPayment\",\"title\":\"Customer Payment\"},{\"id\":\"ExpenseReport\",\"title\":\"Expense Report\"},{\"id\":\"VendorPayment\",\"title\":\"Vendor Payment\"},{\"id\":\"BillCredit\",\"title\":\"Bill Credit\"}]"))}
            )
    })
    @GetMapping(value = "/transaction-types", produces = MediaType.APPLICATION_JSON_VALUE, name = "Transaction types")
    public ResponseEntity<?> transactionType() throws JSONException {

        JSONArray jsonArray = new JSONArray();

        for (TransactionType transactionType : TransactionType.values()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", transactionType);
            jsonObject.put("title", transactionType.name().replaceAll("(\\p{Lower})(\\p{Upper})", "$1 $2"));

            jsonArray.put(jsonObject);
        }

        return ResponseEntity.ok().body(jsonArray.toString());
    }

    @Tag(name = "Transactions", description = "Transactions API")
    @PostMapping(value = "/extraction", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Trigger the extraction from the ERP system(s)", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"event\": \"EXTRACTION\",\"message\":\"We have received your extraction request now. Please review imported transactions from the batch list.\"}"))},
                    responseCode = "202"
            )
    })
    public ResponseEntity<?> extractionTrigger(@Valid @RequestBody ExtractionRequest body) {
        val orgM = accountingCoreResourceService.findOrganisationById(body.getOrganisationId());

        if (orgM.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Unable to find Organisation by Id: \{body.getOrganisationId()}")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        val org = orgM.orElseThrow();

        if (!accountingCoreResourceService.checkFromToDates(org, body.getDateFrom(), body.getDateTo())) {
            val issue = Problem.builder()
                    .withTitle("ORGANISATION_DATE_MISMATCH")
                    .withDetail(STR."the requested data is outside of accounting period for \{body.getOrganisationId()}")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }
        accountingCorePresentationService.extractionTrigger(body);

        JSONObject response = new JSONObject()
                .put("event", "EXTRACTION")
                .put("message", "We have received your extraction request now. Please review imported transactions from the batch list.");

        return ResponseEntity
                .status(HttpStatusCode.valueOf(202))
                .body(response.toString());
    }

    @Tag(name = "Batchs", description = "Batchs API")
    @PostMapping(value = "/batchs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Batch list",
            responses = {
                    @ApiResponse(content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BatchsDetailView.class)))
                    })
            }
    )
    public ResponseEntity<?> listAllBatch(@Valid @RequestBody BatchSearchRequest body,
                                          @RequestParam(name = "page", defaultValue = "1") int page,
                                          @RequestParam(name = "limit", defaultValue = "10") int limit) {
        body.setLimit(limit);
        body.setPage(page);

        val batchs = accountingCorePresentationService.listAllBatch(body);

        return ResponseEntity.ok().body(batchs);
    }

    @Tag(name = "Batchs", description = "Batchs API")
    @GetMapping(value = "/batchs/{batchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Batch detail",
            responses = {
                    @ApiResponse(content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BatchView.class))
                    }),
                    @ApiResponse(responseCode = "404", description = "Error: response status is 404", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"title\": \"BATCH_NOT_FOUND\",\"status\": 404,\"detail\": \"Batch with id: {batchId} could not be found\"" +
                            "}"))})
            }
    )

    public ResponseEntity<?> batchDetail(@Valid @PathVariable("batchId") @Parameter(example = "TESTd12027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04") String batchId) {

        val txBatchM = accountingCorePresentationService.batchDetail(batchId);
        if (txBatchM.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("BATCH_NOT_FOUND")
                    .withDetail(STR."Batch with id: {\{batchId}} could not be found")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        return ResponseEntity.ok().

                body(txBatchM.orElseThrow());
    }

}
