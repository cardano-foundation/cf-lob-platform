CREATE TYPE accounting_core_metric_type AS ENUM (
    'BALANCE_SHEET',
    'INCOME_STATEMENT'
);

CREATE TYPE account_core_submetric_type AS ENUM (
    'ASSET_CATEGORIES',
    'BALANCE_SHEET_OVERVIEW',
    'TOTAL_EXPENSES',
    'INCOME_STREAMS'
);

CREATE TABLE IF NOT EXISTS accounting_core_dashboards (
    id BIGINT PRIMARY KEY,
    organisation_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    x_pos DOUBLE PRECISION,
    y_pos DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    metric accounting_core_metric_type NOT NULL,
    submetric account_core_submetric_type NOT NULL
);