package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lob.dispatching_strategy", name = "type", havingValue = "IMMEDIATE")
public class ImmediateDispatchingStrategy implements DispatchingStrategy {

    @Override
    public Set<TransactionEntity> apply(String organisationId,
                                        Set<TransactionEntity> transactions) {
        return DispatchingStrategy.super.apply(organisationId, transactions);
    }

}
