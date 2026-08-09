package com.example.javalab.practical;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * How to cap concurrency against a limited downstream resource.
 *
 * <pre>
 *   1,000 incoming tasks
 *          |
 *          v
 *   100 virtual threads
 *          |
 *          v
 *   Semaphore limit: 10     &lt;- e.g. 10 DB connections / API quota / Redis slots
 *          |
 *          v
 *   external resource (simulated: 30 ms per call)
 * </pre>
 *
 * <p>Without the semaphore, all 100 virtual threads would hit the resource at
 * once - overloading database connections, HTTP API rate limits, Redis and
 * other downstream services.
 */
public class SemaphoreConcurrencyLimitExample {

    private static final int INCOMING_TASKS = 1_000;
    private static final int SEMAPHORE_LIMIT = 10;      // resource capacity
    private static final long RESOURCE_CALL_MS = 30;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Semaphore Concurrency Limit Example ===");
        System.out.println();
        System.out.println("1,000 incoming tasks, 100 virtual threads, semaphore limit "
                + SEMAPHORE_LIMIT + ".");
        System.out.println("Simulated resource: one call takes " + RESOURCE_CALL_MS + " ms.");
        System.out.println();

        Semaphore slots = new Semaphore(SEMAPHORE_LIMIT);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(INCOMING_TASKS);

        long start = System.nanoTime();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < INCOMING_TASKS; i++) {
                executor.submit(() -> {
                    try {
                        slots.acquire();                  // wait for a free "slot"
                        int now = active.incrementAndGet();
                        maxActive.accumulateAndGet(now, Math::max);
                        Thread.sleep(RESOURCE_CALL_MS);   // simulated resource call
                        active.decrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        slots.release();
                        done.countDown();
                    }
                });
            }
        }
        done.await(120, TimeUnit.SECONDS);
        long wallMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("All " + INCOMING_TASKS + " tasks finished in " + wallMs + " ms.");
        System.out.println("Max simultaneous resource calls: " + maxActive.get()
                + " (never exceeds " + SEMAPHORE_LIMIT + ")");
        System.out.println();
        System.out.println("WHY unlimited concurrency is dangerous:");
        System.out.println("  - Database: a connection pool of 10 cannot serve 1000");
        System.out.println("    simultaneous queries; requests queue in the driver and");
        System.out.println("    time out. The DB itself sees connection storms.");
        System.out.println("  - External HTTP APIs: rate limits (30 RPS, 1000 RPH...)");
        System.out.println("    -> 429 responses, throttling, possible account bans.");
        System.out.println("  - Redis: single-threaded command processing - a burst just");
        System.out.println("    queues up and latency explodes for everyone.");
        System.out.println("  - Any downstream: queueing in THEIR infrastructure.");
        System.out.println();
        System.out.println("Rule: size the Semaphore to the DOWNSTREAM capacity, not to");
        System.out.println("how many threads you can create. This applies to virtual");
        System.out.println("threads AND platform threads.");
    }
}
