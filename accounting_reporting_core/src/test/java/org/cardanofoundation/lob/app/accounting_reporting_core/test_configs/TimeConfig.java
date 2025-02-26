package org.cardanofoundation.lob.app.accounting_reporting_core.test_configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
@Slf4j
public class TimeConfig {

    @Bean
    public Clock clock() {
        // Set the fixed instant and time zone
        log.info("Setting fixed clock for testing...");
        Instant fixedInstant = Instant.parse("2025-02-26T00:00:00.00Z");
        ZoneId zoneId = ZoneId.of("UTC");
        return Clock.fixed(fixedInstant, zoneId);
    }
}
