package org.cardanofoundation.lob.app.support.spring_audit.internal;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }
        return Optional.empty();
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(LocalDateTime.now(clock));
    }

}
