package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.executors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.metrics.MetricExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric.MetricEnum.BALANCE_SHEET;

@Component
@RequiredArgsConstructor
public class BalanceSheetMetricService extends MetricExecutor {

    @PostConstruct
    public void init() {
        name = BALANCE_SHEET;
        metrics = Map.of(
                MetricEnum.SubMetric.ASSET_CATEGORIES, this::getAssetCategories,
                MetricEnum.SubMetric.BALANCE_SHEET_OVERVIEW, this::getBalanceSheetOverview
        );
    }

    private Object getBalanceSheetOverview(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        return Map.of(
                "BalanceOverview", 1000
        );
    }

    private Map<String, Integer> getAssetCategories(String organisationID, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        return Map.of(
                "totalAssets", 1000
        );
    }
}
