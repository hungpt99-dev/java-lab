package com.example.javalab.virtualthread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Blocking I/O workload: 600 "remote calls" of 50 ms each.
 *
 * <p>The exact same blocking code, run two ways:
 * <ul>
 *   <li>a fixed platform pool of 8 threads  -> requests wait in the queue,</li>
 *   <li>one virtual thread per call         -> every request runs at once.</li>
 * </ul>
 *
 * <p>Latency percentiles show the difference: with platform threads, p95 is
 * dominated by QUEUEING; with virtual threads, p95 is just the call time.
 */
public class VirtualThreadIoExample {

    private static final int CALLS = 600;
    private static final long CALL_MS = 50;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads for Blocking I/O ===");
        System.out.println();
        System.out.println("Workload: " + CALLS + " remote calls x " + CALL_MS + " ms each.");
        System.out.println();

        // --- Platform pool of 8 ---
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Long> platformLatencies = new ArrayList<>();
        CountDownLatch done1 = new CountDownLatch(CALLS);
        long start1 = System.nanoTime();
        for (int i = 0; i < CALLS; i++) {
            long submittedAt = System.nanoTime();
            pool.execute(() -> {
                LockSupport.parkNanos(CALL_MS * 1_000_000L);   // simulated remote call
                platformLatencies.add((System.nanoTime() - submittedAt) / 1_000_000);
                done1.countDown();
            });
        }
        done1.await(120, TimeUnit.SECONDS);
        long platformWall = (System.nanoTime() - start1) / 1_000_000;
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // --- Virtual threads: one per call ---
        List<Long> virtualLatencies = new ArrayList<>();
        CountDownLatch done2 = new CountDownLatch(CALLS);
        long start2 = System.nanoTime();
        for (int i = 0; i < CALLS; i++) {
            long submittedAt = System.nanoTime();
            Thread.startVirtualThread(() -> {
                LockSupport.parkNanos(CALL_MS * 1_000_000L);
                virtualLatencies.add((System.nanoTime() - submittedAt) / 1_000_000);
                done2.countDown();
            });
        }
        done2.await(120, TimeUnit.SECONDS);
        long virtualWall = (System.nanoTime() - start2) / 1_000_000;

        System.out.printf("platform pool (8 threads): wall %5d ms, p95 latency %3d ms%n",
                platformWall, percentile(platformLatencies, 95));
        System.out.printf("virtual threads (600):     wall %5d ms, p95 latency %3d ms%n",
                virtualWall, percentile(virtualLatencies, 95));

        System.out.println();
        System.out.println("Observation:");
        System.out.println("With a small platform pool, most of the latency is QUEUEING");
        System.out.println("(waiting for a free thread). With virtual threads, every call");
        System.out.println("runs immediately: p95 ~= the call time itself.");
        System.out.println("This is the sweet spot for HTTP calls, JDBC calls, file I/O");
        System.out.println("and any high-concurrency blocking workload.");
    }

    private static long percentile(List<Long> values, int pct) {
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        return sorted.get((sorted.size() - 1) * pct / 100);
    }
}
