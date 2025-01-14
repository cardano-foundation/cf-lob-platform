package org.cardanofoundation.lob.app.accounting_reporting_core.config;


import org.cardanofoundation.lob.app.support.reactive.DebouncerManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ReactiveConfig {

    @Value("${batch.stats.debounce.duration:PT1M}")
    private Duration debouncerExpireTime;

    @Bean(destroyMethod = "cleanup")
    public DebouncerManager debouncerManager() {
        return new DebouncerManager(debouncerExpireTime);
    }

}
