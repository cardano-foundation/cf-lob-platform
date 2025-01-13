package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BalanceSheetMetricService extends MetricExecutor {

    @PostConstruct
    public void init() {
        name = "BalanceSheet";
        metrics = Map.of(
                "assets", this::getAssets
        );
    }

    private Map<String, Integer> getAssets(Date startDate, Date endDate) {
        return Map.of(
                "totalAssets", 1000
        );
    }
}
