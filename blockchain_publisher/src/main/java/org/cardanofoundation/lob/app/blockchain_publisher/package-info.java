@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Blockchain Publisher",
        allowedDependencies =
                { "accounting_reporting_core::domain_core",
                        "accounting_reporting_core::domain_event_ledger", "accounting_reporting_core::domain_event_reconcilation", "accounting_reporting_core::domain_event_extraction",
                        "organisation", "organisation::domain_core", "organisation::domain_entity",
                        "support::audit",
                        "support::calc",
                        "support::collections",
                        "support::modulith",
                }
)
package org.cardanofoundation.lob.app.blockchain_publisher;
