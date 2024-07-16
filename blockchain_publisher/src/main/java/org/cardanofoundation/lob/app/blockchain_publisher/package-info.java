@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Blockchain Publisher",
        allowedDependencies =
                { "accounting_reporting_core::domain_core",
                        "accounting_reporting_core::domain_event",
                        "organisation", "organisation::domain_core", "organisation::domain_entity",
                        "support::audit",
                        "support::collections",
                }
)
package org.cardanofoundation.lob.app.blockchain_publisher;
