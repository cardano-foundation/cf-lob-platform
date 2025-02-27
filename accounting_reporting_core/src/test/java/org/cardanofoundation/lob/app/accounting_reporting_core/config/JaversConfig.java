package org.cardanofoundation.lob.app.accounting_reporting_core.config;

import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;

import org.cardanofoundation.lob.app.support.javers.LOBBigDecimalComparator;

@Configuration
@Slf4j
public class JaversConfig {

    @Bean
    public Javers javers() {
        log.info("Creating Javers diff instance...");

        return JaversBuilder.javers()
                .withPrettyPrint(true)
                .registerValue(BigDecimal.class, new LOBBigDecimalComparator())
                .build();
    }

}
