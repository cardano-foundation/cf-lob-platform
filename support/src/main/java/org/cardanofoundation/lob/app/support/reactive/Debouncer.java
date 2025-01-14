package org.cardanofoundation.lob.app.support.reactive;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class Debouncer {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> future = null;

    private final Runnable task;

    private final long delay;

    public Debouncer(Runnable task, Duration duration) {
        this.task = task;
        this.delay = duration.toMillis();
    }

    public synchronized void call() {
        if (future != null && !future.isDone()) {
            future.cancel(false); // Cancel the previous task if it is pending.
        }
        future = scheduler.schedule(task, delay, MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1000, MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
