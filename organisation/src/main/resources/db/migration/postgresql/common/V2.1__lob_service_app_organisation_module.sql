CREATE TABLE organisation (
   organisation_id CHAR(64) NOT NULL,
   name VARCHAR(255) NOT NULL,
   tax_id_number VARCHAR(255) NOT NULL,
   country_code VARCHAR(2) NOT NULL,
   dummy_account VARCHAR(255),
   accounting_period_months INT NOT NULL,
   currency_id VARCHAR(255) NOT NULL,
   admin_email VARCHAR(255) NOT NULL,
   pre_approve_transactions BOOLEAN,
   pre_approve_transactions_dispatch BOOLEAN,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation PRIMARY KEY (organisation_id)
);

CREATE TABLE organisation_currency (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   currency_id VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_currency PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE organisation_vat (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   rate DECIMAL NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_vat PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE organisation_cost_center (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   external_customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_cost_center PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE organisation_project (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   external_customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_project PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE organisation_chart_of_account (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   ref_code VARCHAR(255) NOT NULL,
   event_ref_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_chart_of_account PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE organisation_account_event (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_account_event PRIMARY KEY (organisation_id, customer_code)
);
