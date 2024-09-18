package org.cardanofoundation.lob.app.support.spring_audit.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Slf4j
public class AuditDataProvider implements AuditorAware<String>, DateTimeProvider {

    @Override
    public Optional<String> getCurrentAuditor() {
        // log.info("AuditDataProvider.getCurrentAuditor");

//        return Optional.ofNullable(SecurityContextHolder.getContext())
//                .map(SecurityContext::getAuthentication)
//                .filter(Authentication::isAuthenticated)
//                .map(Authentication::getPrincipal)
//                .map(User.class::cast)
//                .map(User::getUsername);

        // TODO find out logged in user
        return Optional.of("system");
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        // log.info("AuditDataProvider.getNow");

        return Optional.of(LocalDateTime.now());
    }

}
