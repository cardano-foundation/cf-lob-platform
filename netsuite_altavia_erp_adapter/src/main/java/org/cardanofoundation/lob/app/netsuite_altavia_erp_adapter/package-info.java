@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "NetSuite Altavia Adapter",
allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "accounting_reporting_core::domain_core", "accounting_reporting_core::domain_event_ledger", "accounting_reporting_core::domain_event_reconcilation", "accounting_reporting_core::domain_event_extraction",
        "accounting_reporting_core::service_business_rules", "accounting_reporting_core::service_assistance",
        "organisation", "organisation::domain_entity",
        "support::modulith",
        "support::spring_audit",
        "support::collections",
        "support::orm",
        "support::crypto",
})
package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter;
