package org.cardanofoundation.lob.app.support.spring_audit.internal;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AuditDataProvider implements AuditorAware<String>, DateTimeProvider {

    private final Clock clock;

    @PostConstruct
    public void init() {
        log.info("AuditDataProvider.init");
    }

    @Override
    public Optional<String> getCurrentAuditor() {

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
        return Optional.of(LocalDateTime.now(clock));
    }

}
