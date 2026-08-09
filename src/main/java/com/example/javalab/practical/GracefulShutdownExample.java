package com.example.javalab.practical;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The correct way to stop an ExecutorService:
 *
 * <pre>
 *   1. shutdown()        - stop accepting new tasks
 *   2. awaitTermination() - give the running tasks a deadline
 *   3. shutdownNow()      - if the deadline passed: interrupt workers,
 *                           drop queued tasks (returned as a List)
 *   4. awaitTermination() again, then give up if still alive
 * </pre>
 *
 * <p>Scenario: a pool of 3 threads, 8 tasks of 2 s each. With a 800 ms
 * termination deadline, only 3 tasks even START; the 5 queued ones are
 * dropped by shutdownNow, and the running workers are interrupted.
 */
public class GracefulShutdownExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Graceful Shutdown Example ===");
        System.out.println();
        System.out.println("Pool of 3 threads, 8 tasks x 2 s. Termination deadline: 800 ms.");
        System.out.println();

        ExecutorService pool = Executors.newFixedThreadPool(3, runnable ->
                new Thread(runnable, "worker"));
        AtomicInteger started = new AtomicInteger();
        AtomicInteger interrupted = new AtomicInteger();

        for (int i = 1; i <= 8; i++) {
            final int id = i;
            pool.execute(() -> {
                started.incrementAndGet();
                try {
                    Thread.sleep(2_000);            // long-running work
                    System.out.println("  task " + id + " completed normally");
                } catch (InterruptedException e) {
                    interrupted.incrementAndGet();
                    System.out.println("  task " + id + " INTERRUPTED during shutdown");
                }
            });
        }

        Thread.sleep(200);    // let the first 3 tasks start

        pool.shutdown();      // 1) no new tasks accepted
        System.out.println("shutdown() called - new submissions will be rejected.");

        boolean finished = pool.awaitTermination(800, TimeUnit.MILLISECONDS);   // 2)
        System.out.println("after 800 ms: all tasks finished? " + finished);

        if (!finished) {
            // 3) deadline passed: interrupt workers, drop queued tasks
            List<Runnable> dropped = pool.shutdownNow();
            System.out.println("shutdownNow() called - interrupted workers and dropped "
                    + dropped.size() + " queued task(s).");
        }

        // 4) wait again for the interrupted workers to finish their cleanup
        boolean cleanedUp = pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("workers cleaned up: " + cleanedUp);
        System.out.println();
        System.out.println("started=" + started.get() + " interrupted=" + interrupted.get());

        System.out.println();
        System.out.println("Observation:");
        System.out.println("Only " + started.get() + " of 8 tasks ever ran - the rest were");
        System.out.println("still queued when shutdownNow dropped them.");
        System.out.println("shutdownNow() interrupts workers; well-behaved tasks catch");
        System.out.println("InterruptedException and clean up (close sockets, release");
        System.out.println("locks, roll back) before exiting.");
        System.out.println("Production pattern: Spring/Quarkus call this for you on");
        System.out.println("graceful shutdown - respect the interrupt flag in your code.");
    }
}
