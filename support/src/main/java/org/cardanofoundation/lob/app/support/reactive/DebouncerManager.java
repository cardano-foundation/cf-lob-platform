package org.cardanofoundation.lob.app.support.reactive;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import lombok.val;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DebouncerManager {

    private final Cache<String, Debouncer> debouncerCache;

    public DebouncerManager(Duration duration) {
        debouncerCache = CacheBuilder.newBuilder()
                .expireAfterAccess(duration.toMillis(), MILLISECONDS)
                .removalListener(notification -> {
                    val debouncer = (Debouncer) notification.getValue();
                    if (debouncer != null) {
                        debouncer.shutdown();
                    }
                })
                .build();
    }

    public Debouncer getDebouncer(String id,
                                  Runnable task,
                                  Duration delay) throws ExecutionException {
        return debouncerCache.get(id, () -> new Debouncer(task, delay));
    }

    public void cleanup() {
        debouncerCache.cleanUp();
    }

}
