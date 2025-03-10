package org.cardanofoundation.lob.app.organisation.resource;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.cardanofoundation.lob.app.organisation.domain.request.ReferenceCodeUpdate;
import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationView;
import org.cardanofoundation.lob.app.organisation.domain.view.ReferenceCodeView;
import org.cardanofoundation.lob.app.organisation.service.OrganisationService;
import org.cardanofoundation.lob.app.organisation.service.ReferenceCodeService;

@RestController
@RequestMapping("/api/organisation/reference-codes")
@Tag(name = "Organisation", description = "Organisation API")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "lob.organisation.enabled", havingValue = "true", matchIfMissing = true)
@PreAuthorize("hasRole(@securityConfig.getManagerRole())")
public class ReferenceCodeResource {

    private final ReferenceCodeService referenceCodeService;
    private final OrganisationService organisationService;

    @Operation(description = "Reference Codes", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationView.class)))}
            ),
    })
    @GetMapping(value = "/{orgId}", produces = "application/json")
    public List<ReferenceCodeView> getReferenceCodes(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {
        return referenceCodeService.getAllReferenceCodes(orgId);
    }

    @Operation(description = "Reference Code update", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationView.class)))}
            ),
    })
    @PostMapping(value = "/{orgId}", produces = "application/json")
    public ResponseEntity<?> upsertReferenceCode(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId,
                                                 @Valid @RequestBody ReferenceCodeUpdate referenceCodeUpdate) {
        Optional<Organisation> organisationChe = organisationService.findById(orgId);
        if (organisationChe.isEmpty()) {
            ThrowableProblem issue = Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Unable to find Organisation by Id: \{orgId}")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }
        return ResponseEntity.ok(referenceCodeService.upsertReferenceCode(orgId, referenceCodeUpdate));
    }

    @Operation(description = "Reference Code delete", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationView.class)))}
            ),
    })
    @DeleteMapping(value = "/{orgId}/{refCode}", produces = "application/json")
    public ResponseEntity<?> deleteReferenceCode(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId,
                                              @PathVariable("refCode") String referenceCode) {
        Optional<Organisation> organisationChe = organisationService.findById(orgId);
        if (organisationChe.isEmpty()) {
            ThrowableProblem issue = Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Unable to find Organisation by Id: \{orgId}")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }
        return null;
    }



}
