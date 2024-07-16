@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "NetSuite Altavia Adapter",
allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "accounting_reporting_core::domain_core", "accounting_reporting_core::domain_event",
        "accounting_reporting_core::service_business_rules",
        "organisation", "organisation::domain_entity",
        "support::audit",
        "support::collections",
        "support::orm",
        "support::crypto",
})
package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter;
