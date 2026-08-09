package com.example.javalab.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * INTENTIONALLY BROKEN example: a shared counter incremented from many
 * threads without any synchronization.
 *
 * <p>{@code count++} is NOT a single operation. It is:
 * <ol>
 *   <li>READ the field into a register</li>
 *   <li>ADD 1</li>
 *   <li>WRITE the field back</li>
 * </ol>
 *
 * <p>Two threads can both READ the same value, both ADD, and both WRITE -
 * one increment is silently lost. This is a <b>race condition</b>.
 *
 * <p>It compiles and runs fine. It only fails under concurrency - which is
 * why race conditions typically surface in production, not in development.
 */
public class RaceConditionExample {

    /** Shared mutable state accessed without synchronization. */
    private int count;

    /** Read-modify-write: NOT atomic. */
    public void increment() {
        count++;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Race Condition Example ===");
        System.out.println();
        System.out.println("8 threads x 50,000 increments -> expected 400,000");
        System.out.println();

        for (int trial = 1; trial <= 5; trial++) {
            RaceConditionExample counter = new RaceConditionExample();
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
                f.get();                    // wait for every task, surface failures
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);

            int expected = threadCount * incrementsPerThread;
            boolean correct = counter.count == expected;
            System.out.printf("Trial %d: expected=%d actual=%d %s%n",
                    trial, expected, counter.count,
                    correct ? "(correct this time)" : "(<-- WRONG: increments lost)");
        }

        System.out.println();
        System.out.println("Observation:");
        System.out.println("count++ = READ + ADD + WRITE. Two threads can read the same");
        System.out.println("value and write it back, losing one increment. The race is");
        System.out.println("nondeterministic: some trials may even look correct.");
        System.out.println();
        System.out.println("Fix: synchronized (SynchronizedExample), ReentrantLock");
        System.out.println("(LockExample), or AtomicInteger (AtomicIntegerExample).");
    }
}
