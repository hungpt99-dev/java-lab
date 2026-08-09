package com.example.javalab.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Compares the four {@link ThreadPoolExecutor} rejection policies when the
 * pool is saturated (all workers busy + queue full).
 *
 * <pre>
 *   configuration: core=1, max=2, queue capacity=1  ->  4 tasks: 3 fit, 1 must be rejected
 * </pre>
 *
 * <p>Policies:
 * <ul>
 *   <li>{@code AbortPolicy} (default) - throws RejectedExecutionException.</li>
 *   <li>{@code CallerRunsPolicy}     - runs the task in the CALLER thread
 *       (natural backpressure: the producer slows down).</li>
 *   <li>{@code DiscardPolicy}        - silently drops the task (data loss!).</li>
 *   <li>{@code DiscardOldestPolicy}  - drops the oldest queued task.</li>
 * </ul>
 */
public class RejectedExecutionExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Rejection Policies Example ===");
        System.out.println();
        System.out.println("core=1, max=2, queue capacity=1 -> submitting 4 tasks");
        System.out.println("means the 4th task must be rejected (or handled).");
        System.out.println();

        // --- 1) AbortPolicy: the 4th task throws ---
        ThreadPoolExecutor abort = new ThreadPoolExecutor(
                1, 2, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                namedFactory("abort"),
                new ThreadPoolExecutor.AbortPolicy());
        System.out.println("1) AbortPolicy (default):");
        submitFour(abort, "   -> submit %d: %s%n");
        abort.shutdownNow();
        System.out.println();

        // --- 2) CallerRunsPolicy: the 4th task runs in the MAIN thread ---
        ThreadPoolExecutor callerRuns = new ThreadPoolExecutor(
                1, 2, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                namedFactory("caller"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        System.out.println("2) CallerRunsPolicy:");
        submitFour(callerRuns, "   -> submit %d: %s%n");
        callerRuns.shutdownNow();

        System.out.println();
        System.out.println("Observation:");
        System.out.println("AbortPolicy fails the request - the caller must handle the");
        System.out.println("exception (e.g. return 503). CallerRunsPolicy slows the producer");
        System.out.println("down instead: the submitting thread itself has to do the work.");
        System.out.println("DiscardPolicy silently loses tasks - never use it for");
        System.out.println("important work. CallerRunsPolicy is the production favorite");
        System.out.println("for backpressure.");
    }

    /** Submits 4 tasks; the 4th exercises the rejection path. */
    private static void submitFour(ThreadPoolExecutor pool, String format) throws InterruptedException {
        AtomicInteger ran = new AtomicInteger();
        for (int i = 1; i <= 4; i++) {
            final int id = i;
            try {
                pool.execute(() -> {
                    ran.incrementAndGet();
                    try {
                        Thread.sleep(400);    // keep workers busy
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                System.out.printf(format, id, "accepted");
            } catch (RejectedExecutionException e) {
                System.out.printf(format, id, "REJECTED: " + e.getClass().getSimpleName());
            }
            Thread.sleep(30);
        }
        System.out.println("   tasks actually executed: " + ran.get());
    }

    private static java.util.concurrent.ThreadFactory namedFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> new Thread(runnable, prefix + "-" + counter.getAndIncrement());
    }
}
