package com.example.javalab.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The lock-free fix for the counter race: {@link AtomicInteger}.
 *
 * <p>Instead of a monitor, {@code AtomicInteger} uses CAS
 * (compare-and-swap) at the hardware level: the update loop reads the value,
 * computes the new one, and atomically swaps it in only if the value has not
 * changed in the meantime - otherwise it retries.
 */
public class AtomicIntegerExample {

    private final AtomicInteger count = new AtomicInteger();

    /** Atomic read-modify-write. No monitor, no blocking. */
    public void increment() {
        count.incrementAndGet();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== AtomicInteger Example ===");
        System.out.println();
        System.out.println("8 threads x 50,000 increments -> expected 400,000");
        System.out.println();

        for (int trial = 1; trial <= 3; trial++) {
            AtomicIntegerExample counter = new AtomicIntegerExample();
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
                    trial, threadCount * incrementsPerThread, counter.count.get(),
                    counter.count.get() == threadCount * incrementsPerThread ? "(correct)" : "(WRONG!)");
        }

        System.out.println();
        System.out.println("Other useful operations:");
        System.out.println("  count.get()                 -> current value");
        System.out.println("  count.getAndIncrement()     -> returns OLD value, then +1");
        System.out.println("  count.addAndGet(n)          -> atomic +n");
        System.out.println("  count.compareAndSet(exp,up) -> only set if unchanged");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("AtomicInteger is correct AND non-blocking - ideal for");
        System.out.println("counters, sequence numbers and small shared state.");
        System.out.println("For complex invariants (multi-field updates), use locks.");
    }
}
