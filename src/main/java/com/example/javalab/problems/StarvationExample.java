package com.example.javalab.problems;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task starvation (head-of-line blocking) in a shared, undersized pool.
 *
 * <p>A pool of 2 threads receives TWO long tasks first. Every worker is
 * occupied, so all later SHORT tasks wait until a long task finishes - even
 * though they would take microseconds on their own.
 *
 * <p>SAFE and deterministic: the long tasks take a fixed 2 seconds, then
 * everything drains and the program exits.
 */
public class StarvationExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Thread Starvation Example ===");
        System.out.println();
        System.out.println("Pool of 2 threads. Timeline:");
        System.out.println("  t=0s   2 long tasks arrive -> occupy BOTH workers");
        System.out.println("  t=0.1s 10 short tasks arrive -> queue behind the long tasks");
        System.out.println("  t=2s   long tasks finish -> short tasks finally run");
        System.out.println();

        int poolSize = 2;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize, runnable ->
                new Thread(runnable, "starvation-worker"));

        CountDownLatch allShortDone = new CountDownLatch(10);
        AtomicInteger shortCount = new AtomicInteger();

        long experimentStart = System.nanoTime();

        // 1) Two long tasks grab both workers.
        for (int i = 1; i <= 2; i++) {
            final int id = i;
            pool.execute(() -> {
                System.out.println("  [long-" + id + "] START (holds a worker for 2s)");
                sleep(2_000);
                System.out.println("  [long-" + id + "] END");
            });
        }

        Thread.sleep(100);

        // 2) Ten short tasks arrive while both workers are busy.
        long firstShortSubmitted = System.nanoTime();
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            pool.execute(() -> {
                long waitMs = (System.nanoTime() - firstShortSubmitted) / 1_000_000;
                System.out.printf("  [short-%02d] ran after waiting ~%4d ms (work itself: <1 ms)%n",
                        id, waitMs);
                shortCount.incrementAndGet();
                allShortDone.countDown();
            });
        }
        long lastShortSubmitted = System.nanoTime();

        // 3) Wait until every short task has run, then measure the worst delay.
        allShortDone.await(10, TimeUnit.SECONDS);
        long worstDelay = (System.nanoTime() - lastShortSubmitted) / 1_000_000;

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println();
        System.out.println("Results:");
        System.out.println("  short tasks executed : " + shortCount.get() + "/10");
        System.out.println("  worst delay for a short task: ~" + worstDelay + " ms");
        System.out.println("  (a short task is real work worth <1 ms; it waited ~2 s)");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("This is 'task starvation' (head-of-line blocking): long tasks");
        System.out.println("at the head of the queue starve everything behind them.");
        System.out.println("Related but different: 'lock starvation' - the default");
        System.out.println("non-fair synchronized monitor may let a thread wait");
        System.out.println("indefinitely under constant contention (fix: fair lock or");
        System.out.println("timeouts). In production: separate pools per workload type,");
        System.out.println("chunk long jobs, and put timeouts on downstream calls.");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
