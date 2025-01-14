package org.cardanofoundation.lob.app.support.spring_audit.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditDataProvider", dateTimeProviderRef = "auditDataProvider")
public class AuditConfig {

    @Bean
    public AuditDataProvider auditDataProvider(Clock clock) {
        return new AuditDataProvider(clock);
    }

}
