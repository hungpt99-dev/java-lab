package com.example.javalab.virtualthread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * THE production lesson of this package:
 *
 * <pre>
 *   1,000 virtual threads
 *          |
 *          v
 *   Semaphore (10 permits)   &lt;- simulates a 10-connection database pool
 *          |
 *          v
 *   simulated database query (50 ms each)
 * </pre>
 *
 * <p>Virtual threads make WAITING cheap - but the database still has only 10
 * connections. Throughput is identical whether the waiters are 10 platform
 * threads or 1,000 virtual threads: the semaphore is the bottleneck.
 *
 * <p>> Virtual Threads remove the cost of waiting threads,
 * > NOT the cost of the resources they are waiting for.
 */
public class VirtualThreadResourceLimitExample {

    private static final int VIRTUAL_THREADS = 400;
    private static final int POOL_LIMIT = 10;         // "database connections"
    private static final long QUERY_MS = 50;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads and Resource Limits ===");
        System.out.println();
        System.out.println("Scenario: " + VIRTUAL_THREADS + " requests, each needing a");
        System.out.println("database query. Connection pool size: " + POOL_LIMIT + ".");
        System.out.println();

        // --- Phase A: 400 virtual threads, limited by a 10-permit semaphore ---
        long limitedTime = phaseA_semaphoreLimit();
        System.out.printf("A) 400 virtual threads + semaphore(10): %4d ms, max parallel = 10%n",
                limitedTime);
        System.out.println();

        // --- Phase B: same work with only 10 PLATFORM threads (no semaphore) ---
        long platformTime = phaseB_tenPlatformThreads();
        System.out.printf("B) 10 platform threads (pool=10):       %4d ms, max parallel = 10%n",
                platformTime);
        System.out.println();

        // --- Phase C: 400 virtual threads WITHOUT any limit ---
        long unlimitedTime = phaseC_unlimitedVirtualThreads();
        System.out.printf("C) 400 virtual threads, NO limit:       %4d ms, max parallel = 400%n",
                unlimitedTime);

        System.out.println();
        System.out.println("==================== KEY MESSAGE ====================");
        System.out.println("A and B take the SAME time: the bottleneck is the 10");
        System.out.println("connections, NOT the threading model. Virtual threads only");
        System.out.println("made the 390 waiting threads nearly free.");
        System.out.println("C 'wins' the timing but would overload the database: 400");
        System.out.println("simultaneous queries against a 10-connection pool means");
        System.out.println("timeouts and queueing inside the pool driver.");
        System.out.println("------------------------------------------------------");
        System.out.println("Virtual Threads remove the cost of waiting threads,");
        System.out.println("NOT the cost of the resources they are waiting for.");
        System.out.println("Always cap concurrency (Semaphore) at the resource limit.");
    }

    /** A) 400 virtual threads, semaphore limits how many hit the "database" at once. */
    private static long phaseA_semaphoreLimit() throws InterruptedException {
        Semaphore connections = new Semaphore(POOL_LIMIT);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(VIRTUAL_THREADS);

        long start = System.nanoTime();
        for (int i = 0; i < VIRTUAL_THREADS; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    connections.acquire();                 // wait for a "connection"
                    int now = active.incrementAndGet();
                    maxActive.accumulateAndGet(now, Math::max);
                    Thread.sleep(QUERY_MS);                // simulated query
                    active.decrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    connections.release();
                    done.countDown();
                }
            });
        }
        done.await(120, TimeUnit.SECONDS);
        System.out.println("   max parallel queries observed: " + maxActive.get());
        return (System.nanoTime() - start) / 1_000_000;
    }

    /** B) exactly 10 platform threads - the same concurrency, the hard way. */
    private static long phaseB_tenPlatformThreads() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(POOL_LIMIT);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(VIRTUAL_THREADS);

        long start = System.nanoTime();
        for (int i = 0; i < VIRTUAL_THREADS; i++) {
            pool.execute(() -> {
                int now = active.incrementAndGet();
                maxActive.accumulateAndGet(now, Math::max);
                try {
                    Thread.sleep(QUERY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                active.decrementAndGet();
                done.countDown();
            });
        }
        done.await(120, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("   max parallel queries observed: " + maxActive.get());
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return ms;
    }

    /** C) virtual threads with NO limit: everything runs at once (bad for the DB!). */
    private static long phaseC_unlimitedVirtualThreads() throws InterruptedException {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(VIRTUAL_THREADS);

        long start = System.nanoTime();
        for (int i = 0; i < VIRTUAL_THREADS; i++) {
            Thread.startVirtualThread(() -> {
                int now = active.incrementAndGet();
                maxActive.accumulateAndGet(now, Math::max);
                try {
                    Thread.sleep(QUERY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                active.decrementAndGet();
                done.countDown();
            });
        }
        done.await(120, TimeUnit.SECONDS);
        System.out.println("   max parallel queries observed: " + maxActive.get());
        return (System.nanoTime() - start) / 1_000_000;
    }
}
