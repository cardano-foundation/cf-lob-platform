package org.cardanofoundation.lob.app.organisation.resource;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationCostCenterView;
import org.cardanofoundation.lob.app.organisation.domain.view.OrganisationView;
import org.cardanofoundation.lob.app.organisation.service.OrganisationCurrencyService;
import org.cardanofoundation.lob.app.organisation.service.OrganisationService;

@RestController
@RequestMapping("/api")
@Tag(name = "Organisation", description = "Organisation API")
@RequiredArgsConstructor
public class OrganisationResource {

    private final OrganisationService organisationService;
    private final OrganisationCurrencyService organisationCurrencyService;

    @Operation(description = "Transaction types", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationView.class)))}
            ),
    })
    @GetMapping(value = "/organisation", produces = "application/json")
    public ResponseEntity<?> organisationList() {
        return ResponseEntity.ok().body(
                organisationService.findAll().stream().map(organisation -> {
                    val today = LocalDate.now();
                    val monthsAgo = today.minusMonths(organisation.getAccountPeriodDays());
                    val yesterday = today.minusDays(1);

                    return new OrganisationView(
                            organisation.getId(),
                            organisation.getName(),
                            organisation.getTaxIdNumber(),
                            organisation.getCurrencyId(),
                            monthsAgo,
                            yesterday,
                            organisation.getAdminEmail(),
                            new LinkedHashSet<>(),
                            new LinkedHashSet<>(),
                            new LinkedHashSet<>(),
                            organisation.getLogo()
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
            LocalDate monthsAgo = today.minusMonths(organisation1.getAccountPeriodDays());
            LocalDate yesterday = today.minusDays(1);

            return new OrganisationView(
                    organisation1.getId(),
                    organisation1.getName(),
                    organisation1.getTaxIdNumber(),
                    organisation1.getCurrencyId(),
                    monthsAgo,
                    yesterday,
                    organisation1.getAdminEmail(),
                    organisationService.getAllCostCenter(organisation1.getId()).stream().map(organisationCostCenter -> {
                        return new OrganisationCostCenterView(
                                organisationCostCenter.getId() != null ? organisationCostCenter.getId().getCustomerCode() : null,
                                organisationCostCenter.getExternalCustomerCode(),
                                organisationCostCenter.getName()
                        );
                    }).collect(Collectors.toSet()),
                    organisationService.getAllProjects(organisation1.getId()).stream().map(organisationProject -> {
                        return new OrganisationCostCenterView(
                                organisationProject.getId() != null ? organisationProject.getId().getCustomerCode() : null,
                                organisationProject.getExternalCustomerCode(),
                                organisationProject.getName()
                        );
                    }).collect(Collectors.toSet()),
                    organisationCurrencyService.findAllByOrganisationId("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                            .stream()
                            .map(organisationCurrency -> {
                                return organisationCurrency.getId() != null ? organisationCurrency.getId().getCustomerCode() : null;
                            }).collect(Collectors.toSet()),
                    organisation1.getLogo()
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

    @Operation(description = "Organisation cost center", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationCostCenterView.class)))}
            ),
    })
    @GetMapping(value = "/organisation/{orgId}/cost-center", produces = "application/json")
    public ResponseEntity<?> organisationCostCenter(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {
        return ResponseEntity.ok().body(
                organisationService.getAllCostCenter(orgId).stream().map(organisationCostCenter -> {
                    return new OrganisationCostCenterView(
                            organisationCostCenter.getId() != null ? organisationCostCenter.getId().getCustomerCode() : null,
                            organisationCostCenter.getExternalCustomerCode(),
                            organisationCostCenter.getName()
                    );
                }).toList());

    }

    @Operation(description = "Organisation cost center", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationCostCenterView.class)))}
            ),
    })
    @GetMapping(value = "/organisation/{orgId}/project", produces = "application/json")
    public ResponseEntity<?> organisationProject(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {
        return ResponseEntity.ok().body(
                organisationService.getAllProjects(orgId).stream().map(organisationProject -> {
                    return new OrganisationCostCenterView(
                            organisationProject.getId() != null ? organisationProject.getId().getCustomerCode() : null,
                            organisationProject.getExternalCustomerCode(),
                            organisationProject.getName()
                    );
                }).toList());

    }

}
