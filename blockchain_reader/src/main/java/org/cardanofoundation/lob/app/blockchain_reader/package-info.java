@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Blockchain Reader", allowedDependencies = {
        "blockchain_common",
        "blockchain_common::domain",
        "support::spring_audit",
})
package org.cardanofoundation.lob.app.blockchain_reader;
