package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lob.dispatching_strategy", name = "type", havingValue = "IMMEDIATE")
public class ImmediateDispatchingStrategy<T> implements DispatchingStrategy<T> {

    @Override
    public Set<T> apply(String organisationId,
                        Set<T> entries) {
        return DispatchingStrategy.super.apply(organisationId, entries);
    }

}
