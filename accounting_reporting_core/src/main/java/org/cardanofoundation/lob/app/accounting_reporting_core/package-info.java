@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Accounting Service Layer", allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "organisation", "organisation::domain_core", "organisation::domain_entity",
        "blockchain_reader",
        "support::spring_audit",
        "support::calc",
        "support::problem_support",
        "support::crypto",
        "support::modulith",
        "support::collections",
        "support::reactive"
})
package org.cardanofoundation.lob.app.accounting_reporting_core;
