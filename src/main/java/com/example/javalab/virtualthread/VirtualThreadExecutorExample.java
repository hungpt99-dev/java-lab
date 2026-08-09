package com.example.javalab.virtualthread;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * {@code Executors.newVirtualThreadPerTaskExecutor()} - one virtual thread
 * per submitted task, automatically.
 *
 * <p>10,000 tasks that each "work" for 10 ms. Executed sequentially they
 * would take 100 seconds. On a virtual-thread-per-task executor, ALL of them
 * run at once and the whole batch finishes in ~10 ms plus overhead.
 *
 * <p>{@code close()} (or {@code shutdown()}) waits for all submitted tasks,
 * so the try-with-resources block below is a complete, clean program.
 */
public class VirtualThreadExecutorExample {

    public static void main(String[] args) {
        System.out.println("=== newVirtualThreadPerTaskExecutor Example ===");
        System.out.println();
        System.out.println("10,000 tasks, each sleeping 10 ms (simulated blocking work).");
        System.out.println("Sequential: 100 seconds. Virtual threads: ~10 ms.");
        System.out.println();

        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();

        long start = System.nanoTime();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> executor.submit(() -> {
                int now = active.incrementAndGet();
                maxActive.accumulateAndGet(now, Math::max);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                active.decrementAndGet();
            }));
        }   // close() == shutdown() + awaitTermination: waits for all tasks

        long wallMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("All 10,000 tasks completed.");
        System.out.println("Wall time: " + wallMs + " ms");
        System.out.println("Max concurrently running: " + maxActive.get()
                + " (near 10,000 - they all run at once)");
        System.out.println();
        System.out.println("Compare: the same code with a fixed 8-thread platform pool");
        System.out.println("would take ~12.5 s. Virtual threads make blocking code");
        System.out.println("massively concurrent with ZERO pool sizing.");
        System.out.println();
        System.out.println("Warning: 'unlimited concurrency' is a footgun - if these");
        System.out.println("tasks hit a database, the database still has a connection");
        System.out.println("limit. See VirtualThreadResourceLimitExample.");
    }
}
