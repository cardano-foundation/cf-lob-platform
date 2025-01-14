package org.cardanofoundation.lob.app.support.spring_web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.zalando.problem.jackson.ProblemModule;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Configuration
@Slf4j
public class JsonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(ProblemModule problem) {
        log.info("Configuring Jackson2ObjectMapperBuilder");

        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToEnable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, FAIL_ON_UNKNOWN_PROPERTIES)
                .modulesToInstall(new JavaTimeModule(), new Jdk8Module(), problem);
    }

}
