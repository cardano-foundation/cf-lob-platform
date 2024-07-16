package org.cardanofoundation.lob.app.organisation.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationView;
import org.cardanofoundation.lob.app.organisation.service.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Organisation", description = "Organisation API")
@RequiredArgsConstructor
public class OrganisationResource {

    private final OrganisationService organisationService;

    @Operation(description = "Transaction types", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationView.class)))}
            ),
    })
    @GetMapping(value = "/organisation", produces = "application/json")
    public ResponseEntity<?> organisationList() {
        return ResponseEntity.ok().body(
                organisationService.findAll().stream().map(organisation -> {
                    LocalDate today = LocalDate.now();
                    LocalDate monthsAgo = today.minusMonths(organisation.getAccountPeriodMonths());
                    LocalDate yesterday = today.minusDays(1);

                    return new OrganisationView(
                            organisation.getId(),
                            organisation.getName(),
                            organisation.getTaxIdNumber(),
                            organisation.getCurrencyId(),
                            monthsAgo,
                            yesterday,
                            organisation.getAdminEmail()
                    );
                }).toList()
        );
    }

    @Operation(description = "Transaction types", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationView.class))}
            ),
            @ApiResponse(responseCode = "404", description = "Error: response status is 404", content = {@Content(mediaType = "application/json", schema = @Schema(example = "{\n" +
                    "    \"title\": \"Organisation not found\",\n" +
                    "    \"status\": 404,\n" +
                    "    \"detail\": \"Unable to get the organisation\"\n" +
                    "}"))})
    })
    @GetMapping(value = "/organisation/{orgId}", produces = "application/json")
    public ResponseEntity<?> organisationDetailSpecific(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {
        Optional<OrganisationView> organisation = organisationService.findById(orgId).map(organisation1 -> {
            LocalDate today = LocalDate.now();
            LocalDate monthsAgo = today.minusMonths(organisation1.getAccountPeriodMonths());
            LocalDate yesterday = today.minusDays(1);

            return new OrganisationView(
                    organisation1.getId(),
                    organisation1.getName(),
                    organisation1.getTaxIdNumber(),
                    organisation1.getCurrencyId(),
                    monthsAgo,
                    yesterday,
                    organisation1.getAdminEmail()
            );
        });
        if (organisation.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Unable to find Organisation by Id: \{orgId}")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        return ResponseEntity.ok().body(organisation);
    }

}
