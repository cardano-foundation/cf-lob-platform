package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.metric.GetMetricDataRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.metric.SaveDashboardRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.response.metric.MetricDataResponse;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.DashboardView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.metric.MetricView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class MetricController {

    private final MetricService metricService;

    @Tag(name = "Metrics", description = "Available Metrics")
    @GetMapping(value = "/availableMetrics", produces = "application/json")
    public ResponseEntity<MetricView> availableDashboards() {
        return ResponseEntity.ok(new MetricView(metricService.getAvailableMetrics()));
    }

    @Tag(name = "Metrics", description = "Get Data from Metrics")
    @PostMapping(value = "/data", produces = "application/json")
    public ResponseEntity<MetricDataResponse> getDashboardData(@RequestBody GetMetricDataRequest getMetricDataRequest) {
        return ResponseEntity.ok(new MetricDataResponse(metricService.getData(
                getMetricDataRequest.getMetricView().getMetrics(),
                getMetricDataRequest.getOrganisationID(),
                Optional.ofNullable(getMetricDataRequest.getStartDate()),
                Optional.ofNullable(getMetricDataRequest.getEndDate()))));
    }

    @Tag(name = "Save Dashboards", description = "Save Dashboards")
    @PostMapping(value = "/saveDashboard", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> saveDashboard(@RequestBody SaveDashboardRequest saveDashboardRequest) {
        metricService.saveDashboard(saveDashboardRequest.getDashboards(), saveDashboardRequest.getOrganisationID());
        return ResponseEntity.ok().build();
    }

    @Tag(name = "Get Dashboards", description = "Get Dashboards")
    @GetMapping(value = "/dashboards/{organisationID}", produces = "application/json")
    public ResponseEntity<List<DashboardView>> getDashboards(@PathVariable("organisationID") String organisationID) {
        return ResponseEntity.ok(metricService.getAllDashboards(organisationID));
    }
}
