package com.example.javalab.virtualthread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Platform threads vs virtual threads on the SAME blocking workload.
 *
 * <p>Workload: 1,000 tasks, each "blocking" for 30 ms (simulated remote call).
 *
 * <pre>
 *   platform pool of 16 threads -> 1000 / 16 * 30 ms = ~1900 ms
 *   virtual threads (per task)  -> ~30 ms  (all 1000 wait concurrently)
 * </pre>
 *
 * <p>Also creates 100,000 virtual threads that do nothing: this is safe and
 * nearly free, while 100,000 platform threads would usually kill the process
 * (OutOfMemoryError: unable to create native thread).
 */
public class PlatformVsVirtualThreadExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Platform vs Virtual Threads Example ===");
        System.out.println();
        System.out.println("Workload: 1,000 tasks x 30 ms simulated blocking call.");
        System.out.println();

        // --- Platform threads: fixed pool of 16 ---
        ExecutorService platformPool = Executors.newFixedThreadPool(16);
        CountDownLatch done1 = new CountDownLatch(1_000);
        long start1 = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            platformPool.execute(() -> {
                block(30);
                done1.countDown();
            });
        }
        done1.await(120, TimeUnit.SECONDS);
        long platformMs = (System.nanoTime() - start1) / 1_000_000;
        platformPool.shutdown();
        platformPool.awaitTermination(5, TimeUnit.SECONDS);

        // --- Virtual threads: one per task ---
        CountDownLatch done2 = new CountDownLatch(1_000);
        long start2 = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            Thread.startVirtualThread(() -> {
                block(30);
                done2.countDown();
            });
        }
        done2.await(120, TimeUnit.SECONDS);
        long virtualMs = (System.nanoTime() - start2) / 1_000_000;

        System.out.printf("  platform pool (16 threads): %5d ms%n", platformMs);
        System.out.printf("  virtual threads (1000):     %5d ms%n", virtualMs);
        System.out.println();

        // --- Scale check: how many can we CREATE? ---
        long start3 = System.nanoTime();
        int huge = 100_000;
        IntStream.range(0, huge).forEach(i ->
                Thread.startVirtualThread(() -> { /* do nothing */ }));
        long createMs = (System.nanoTime() - start3) / 1_000_000;
        System.out.println("Created " + huge + " idle virtual threads in ~"
                + createMs + " ms - no problem.");
        System.out.println("Creating " + huge + " PLATFORM threads would likely throw");
        System.out.println("OutOfMemoryError: unable to create native thread (~1 MB stack each).");

        System.out.println();
        System.out.println("Observation:");
        System.out.println("Virtual threads win for BLOCKING workloads: waiting is nearly");
        System.out.println("free, so concurrency can scale to the task count itself.");
        System.out.println("They do NOT create more CPU: CPU-bound work is unaffected");
        System.out.println("(see VirtualThreadCpuBoundExample).");
    }

    private static void block(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
