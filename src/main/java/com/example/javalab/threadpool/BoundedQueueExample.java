package com.example.javalab.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contrasts UNBOUNDED vs BOUNDED work queues on the same core/max settings.
 *
 * <p>Both pools below have core=2, max=4 and receive 6 tasks that sleep.
 * The only difference is the queue:
 *
 * <pre>
 *   unbounded queue -> pool stays at 2 threads (max=4 is dead config!)
 *   bounded queue   -> pool grows to 4 threads once the queue fills
 * </pre>
 */
public class BoundedQueueExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Bounded vs Unbounded Queue Example ===");
        System.out.println();

        System.out.println("A) UNBOUNDED queue (LinkedBlockingQueue) - what newFixedThreadPool uses");
        ThreadPoolExecutor unbounded = new ThreadPoolExecutor(
                2, 4, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),                       // never fills
                namedFactory("unbounded"));
        submitSleepingTasks(unbounded, 6);
        Thread.sleep(500);
        System.out.printf("   -> poolSize=%d queueSize=%d (max=4 was NEVER reached!)%n%n",
                unbounded.getPoolSize(), unbounded.getQueue().size());

        System.out.println("B) BOUNDED queue (ArrayBlockingQueue capacity=2)");
        ThreadPoolExecutor bounded = new ThreadPoolExecutor(
                2, 4, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),                       // fills after 2 queued
                namedFactory("bounded"));
        submitSleepingTasks(bounded, 6);
        Thread.sleep(500);
        System.out.printf("   -> poolSize=%d queueSize=%d (pool grew to 4)%n%n",
                bounded.getPoolSize(), bounded.getQueue().size());

        System.out.println("Observation:");
        System.out.println("With an unbounded queue, tasks pile up in memory forever and");
        System.out.println("maximumPoolSize never engages - the queue IS the real limit.");
        System.out.println("A bounded queue forces the pool to engage extra threads, then");
        System.out.println("to apply the rejection policy (backpressure). Always bound");
        System.out.println("queues in production - or cap them in the rejection policy.");

        unbounded.shutdownNow();
        bounded.shutdownNow();
    }

    private static void submitSleepingTasks(ThreadPoolExecutor pool, int count) {
        for (int i = 1; i <= count; i++) {
            final int id = i;
            pool.execute(() -> {
                try {
                    Thread.sleep(1_500);    // long enough to observe pool growth
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private static java.util.concurrent.ThreadFactory namedFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> new Thread(runnable, prefix + "-" + counter.getAndIncrement());
    }
}
