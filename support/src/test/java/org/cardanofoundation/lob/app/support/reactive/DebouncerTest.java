package org.cardanofoundation.lob.app.support.reactive;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DebouncerTest {

    private Runnable task;
    private Debouncer debouncer;

    @BeforeEach
    public void setUp() {
        task = mock(Runnable.class);
        debouncer = new Debouncer(task, Duration.ofMillis(100)); // Using a short delay for testing
    }

    @AfterEach
    public void tearDown() {
        debouncer.shutdown();
    }

    @Test
    public void shouldExecuteOnlyLastInvocation() throws InterruptedException {
        // Call the debouncer method multiple times
        debouncer.call();
        debouncer.call();
        debouncer.call();
        debouncer.call();

        // Allow some time for the debounced execution
        MILLISECONDS.sleep(200);

        // Verify that the task is executed only once
        verify(task, times(1)).run();
    }

    @Test
    public void shouldCancelPreviousTasks() throws InterruptedException {
        debouncer.call();
        MILLISECONDS.sleep(50); // Wait less than the debouncing delay
        debouncer.call();

        MILLISECONDS.sleep(200); // Wait more than the debouncing delay

        // Verify that the task is executed only once, ensuring the first call was cancelled
        verify(task, times(1)).run();
    }

//    @Test
//    public void shouldHandleShutdownCorrectly() throws InterruptedException {
//        debouncer.call();
//        debouncer.shutdown();
//
//        // Allow some time to see if any tasks run after shutdown
//        MILLISECONDS.sleep(100);
//
//        // Verify that the task is not executed after shutdown
//        verify(task, never()).run();
//    }

}
