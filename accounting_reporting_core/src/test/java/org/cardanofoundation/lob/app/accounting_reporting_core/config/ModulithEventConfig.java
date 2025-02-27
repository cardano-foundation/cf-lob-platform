package org.cardanofoundation.lob.app.accounting_reporting_core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.core.EventSerializer;

@Configuration
public class ModulithEventConfig {

    @Bean
    public EventSerializer eventSerializer() {
        return new EventSerializer() {
            @Override
            public Object serialize(Object event) {
                return null;
            }

            @Override
            public <T> T deserialize(Object serialized, Class<T> type) {
                return null;
            }
        };
    }
}
