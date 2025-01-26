package org.cardanofoundation.lob.app.support.spring_web;

import lombok.Getter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration("securityConfig")
@EnableMethodSecurity(prePostEnabled = false)
@Getter
@ConditionalOnProperty(value = "keycloak.enabled", havingValue = "false")
public class DisabledSecurity {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // allowing all access
                );
        return http.build();
    }

}
