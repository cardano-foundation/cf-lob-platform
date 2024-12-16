package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import java.util.Set;
import java.util.function.BiFunction;

public interface DispatchingStrategy<T> extends BiFunction<String, Set<T>, Set<T>> {

    default Set<T> apply(String organisationId, Set<T> entries) {
        return entries;
    }

}
