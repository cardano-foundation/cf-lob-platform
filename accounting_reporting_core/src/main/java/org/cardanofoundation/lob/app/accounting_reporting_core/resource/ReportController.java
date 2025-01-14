package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.presentation_layer_service.ReportViewService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReportResponseView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ReportingParametersView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.ReportService;
import org.cardanofoundation.lob.app.organisation.service.OrganisationCurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    private final ReportViewService reportViewService;
    private final OrganisationCurrencyService organisationCurrencyService;
    private final ReportService reportService;


    @Tag(name = "Reporting", description = "Report Parameters")
    @GetMapping(value = "/report-parameters/{orgId}", produces = "application/json")
    public ResponseEntity<?> reportParameters(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {

        HashMap<String, String> currencyOrg = new HashMap<>();
        /** Todo: Get the value from organisation */
        organisationCurrencyService.findByOrganisationIdAndCode(orgId, "CHF")
                .stream()
                .map(organisationCurrency -> {
                    return currencyOrg.put(organisationCurrency.getCurrencyId(), organisationCurrency.getId().getCustomerCode());
                }).collect(Collectors.toSet());
        return ResponseEntity.ok().body(
                new ReportingParametersView(
                        Arrays.stream(ReportType.values()).collect(Collectors.toSet()),
                        currencyOrg,
                        "2023"
                )
        );
    }


    @Tag(name = "Reporting", description = "Create Balance Sheet")
    @PostMapping(value = "/report-create", produces = "application/json")
    public ResponseEntity<?> reportCreate(@Valid @RequestBody ReportRequest reportSaveRequest) {

        return reportViewService.reportCreate(reportSaveRequest)
                .fold(problem -> {
                    return ResponseEntity.status(problem.getStatus().getStatusCode()).body(ReportResponseView.createFail(problem));
                }, success -> {
                    return ResponseEntity.ok().body(
                            ReportResponseView.createSuccess(Set.of(reportViewService.responseView(success)))
                    );
                });
    }


    @Tag(name = "Reporting", description = "Create Income Statement")
    @PostMapping(value = "/report-search", produces = "application/json")
    public ResponseEntity<?> reportSearch(@Valid @RequestBody ReportSearchRequest reportSearchRequest) {

        return reportService.exist(
                reportSearchRequest.getOrganisationId(),
                reportSearchRequest.getReportType(),
                reportSearchRequest.getIntervalType(),
                reportSearchRequest.getYear(),
                reportSearchRequest.getPeriod()
        ).fold(problem -> {
            return ResponseEntity.status(problem.getStatus().getStatusCode()).body(ReportResponseView.createFail(problem));
        }, success -> {
            return ResponseEntity.ok().body(
                    ReportResponseView.createSuccess(Set.of(reportViewService.responseView(success)))
            );
        });
    }


    @Tag(name = "Reporting", description = "Report list")
    @GetMapping(value = "/report-list/{orgId}", produces = "application/json")
    public ResponseEntity<?> reportList(@PathVariable("orgId") @Parameter(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94") String orgId) {

        return ResponseEntity.ok().body(ReportResponseView.createSuccess(reportService.findByOrgId(
                        orgId
                ).stream().map(reportViewService::responseView).collect(Collectors.toSet()))
        );

    }


    @Tag(name = "Reporting", description = "Report publish")
    @PostMapping(value = "/report-publish", produces = "application/json")
    public ResponseEntity<?> reportPublish(@Valid @RequestBody ReportPublishRequest reportPublishRequest) {

        return reportViewService.reportPublish(reportPublishRequest).fold(
                problem -> {
                    return ResponseEntity.status(problem.getStatus().getStatusCode()).body(ReportResponseView.createFail(problem));
                }, success -> {
                    return ResponseEntity.ok().body(
                            ReportResponseView.createSuccess(Set.of(reportViewService.responseView(success)))
                    );
                }
        );
    }

}
