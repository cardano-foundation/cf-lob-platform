CREATE TABLE IF NOT EXISTS organisation (
   organisation_id CHAR(64) NOT NULL,
   name VARCHAR(255) NOT NULL,
   tax_id_number VARCHAR(255) NOT NULL,
   country_code VARCHAR(2) NOT NULL,
   dummy_account VARCHAR(255),
   accounting_period_days INT NOT NULL,
   currency_id VARCHAR(255) NOT NULL,
   admin_email VARCHAR(255) NOT NULL,
   pre_approve_transactions BOOLEAN,
   pre_approve_transactions_dispatch BOOLEAN,
   logo text,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation PRIMARY KEY (organisation_id)
);

CREATE TABLE IF NOT EXISTS organisation_aud (
   organisation_id CHAR(64) NOT NULL,
   name VARCHAR(255) NOT NULL,
   tax_id_number VARCHAR(255) NOT NULL,
   country_code VARCHAR(2) NOT NULL,
   dummy_account VARCHAR(255),
   accounting_period_days INT NOT NULL,
   currency_id VARCHAR(255) NOT NULL,
   admin_email VARCHAR(255) NOT NULL,
   pre_approve_transactions BOOLEAN,
   pre_approve_transactions_dispatch BOOLEAN,
   logo TEXT,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_organisation_aud PRIMARY KEY (organisation_id, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS organisation_currency (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   currency_id VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_currency PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE IF NOT EXISTS organisation_currency_aud (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   currency_id VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_organisation_currency_aud PRIMARY KEY (organisation_id, customer_code, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS organisation_vat (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   rate DECIMAL(12, 8) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_vat PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE IF NOT EXISTS organisation_vat_aud (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   rate DECIMAL(12, 8) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_organisation_vat_aud PRIMARY KEY (organisation_id, customer_code, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS organisation_cost_center (
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

CREATE TABLE IF NOT EXISTS organisation_cost_center_aud (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   external_customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_organisation_cost_center_aud PRIMARY KEY (organisation_id, customer_code, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS organisation_project (
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

CREATE TABLE IF NOT EXISTS organisation_project_aud (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   external_customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_organisation_project_aud PRIMARY KEY (organisation_id, customer_code, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS organisation_chart_of_account (
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

CREATE TABLE IF NOT EXISTS organisation_chart_of_account_aud (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   ref_code VARCHAR(255) NOT NULL,
   event_ref_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_chart_of_account_aud PRIMARY KEY (organisation_id, customer_code, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS organisation_account_event (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   CONSTRAINT pk_organisation_account_event PRIMARY KEY (organisation_id, customer_code)
);

CREATE TABLE IF NOT EXISTS organisation_account_event_aud (
   organisation_id CHAR(64) NOT NULL,
   customer_code VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT,

   -- Primary Key for the audit table
   CONSTRAINT pk_organisation_account_event_aud PRIMARY KEY (organisation_id, customer_code, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);
