package org.cardanofoundation.lob.app.support.spring_web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class SpringWebConfig {

    @Value("${lob.cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @PostConstruct
    public void init() {
        log.info("CORS configured allowed origins: {}", allowedOrigins);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("GET", "HEAD", "POST") // expose only GET, HEAD, POST
                        .allowedHeaders("*");
            }
        };
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Lob Service")
                        .license(new License().name("Apache License 2.0")
                        .url("https://github.com/cardano-foundation/cf-lob/blob/main/LICENSE")));
    }

}
