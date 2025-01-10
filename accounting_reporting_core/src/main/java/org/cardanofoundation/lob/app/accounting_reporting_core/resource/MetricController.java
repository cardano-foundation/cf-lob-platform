package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.MetricView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class MetricController {

    private final MetricService metricService;

    @Tag(name = "Dashboard", description = "Available Dashboards")
    @GetMapping(value = "/availableMetrics", produces = "application/json")
    public ResponseEntity<MetricView> availableDashboards() {
        return ResponseEntity.ok(new MetricView(metricService.getAvailableMetrics()));
    }

    @Tag(name = "Dashboard", description = "Get Data for Dashboard")
    @PostMapping(value = "/data", produces = "application/json")
    public ResponseEntity<Map<String, List<Object>>> getDashboardData(@RequestBody MetricView metricView) {
        return ResponseEntity.ok(metricService.getData(metricView.getMetrics()));
    }
}
