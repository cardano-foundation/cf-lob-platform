package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.metric;

public enum MetricEnum {

    BALANCE_SHEET,
    INCOME_STATEMENT;

    public enum SubMetric {
        ASSET_CATEGORIES,
        BALANCE_SHEET_OVERVIEW,
        TOTAL_EXPENSES,
        INCOME_STREAMS
    }

}
