package org.cardanofoundation.lob.app.support.audit.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditDataProvider", dateTimeProviderRef = "auditDataProvider")
public class AuditConfig {

    @Bean
    public AuditDataProvider auditDataProvider() {
        return new AuditDataProvider();
    }

}
