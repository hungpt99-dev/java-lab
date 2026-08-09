package com.example.javalab.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The same counter as {@link RaceConditionExample}, but every mutation is
 * guarded by {@code synchronized}.
 *
 * <p>{@code synchronized} provides two guarantees:
 * <ol>
 *   <li><b>Mutual exclusion</b> (atomicity): only one thread at a time runs
 *       the critical section.</li>
 *   <li><b>Visibility</b>: writes inside the block are published to other
 *       threads via a happens-before relationship.</li>
 * </ol>
 */
public class SynchronizedExample {

    private int count;

    /** The monitor makes the read-modify-write a single indivisible unit. */
    public synchronized void increment() {
        count++;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== synchronized Example ===");
        System.out.println();
        System.out.println("8 threads x 50,000 increments -> expected 400,000");
        System.out.println();

        for (int trial = 1; trial <= 3; trial++) {
            SynchronizedExample counter = new SynchronizedExample();
            int threadCount = 8;
            int incrementsPerThread = 50_000;

            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(pool.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);

            System.out.printf("Trial %d: expected=%d actual=%d %s%n",
                    trial, threadCount * incrementsPerThread, counter.count,
                    counter.count == threadCount * incrementsPerThread ? "(correct)" : "(WRONG!)");
        }

        System.out.println();
        System.out.println("Observation:");
        System.out.println("With synchronized, every trial is correct. The cost is that");
        System.out.println("contending threads BLOCK - heavy contention on one monitor");
        System.out.println("turns the code effectively single-threaded.");
        System.out.println("synchronized is reentrant, non-fair by default, and cannot");
        System.out.println("time out. For timeouts, see LockExample (tryLock).");
    }
}
