package com.example.javalab.threadpool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulates a pool where ALL workers get stuck on a slow external resource,
 * while new tasks keep arriving and piling up in the queue.
 *
 * <p>This is the classic production incident: a burst of slow calls saturates
 * the pool; queue depth grows; latency explodes; memory grows - and no error
 * is ever thrown because the queue is unbounded.
 *
 * <p>The latch plays the role of the slow downstream service (database, API).
 * Everything is released afterwards so the example terminates cleanly.
 */
public class ThreadPoolExhaustionExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Thread Pool Exhaustion Example ===");
        System.out.println();
        System.out.println("Pool of 2 threads. 6 tasks 'call' a slow downstream that is");
        System.out.println("blocked (simulated outage). Meanwhile a FAST task arrives.");
        System.out.println();

        int poolSize = 2;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize, runnable ->
                new Thread(runnable, "exhausted-worker"));

        CountDownLatch downstreamDown = new CountDownLatch(1);   // downstream is "down"
        AtomicInteger active = new AtomicInteger();

        // 6 slow tasks: all of them block on the latch (simulated 2s outage).
        for (int i = 1; i <= 6; i++) {
            final int id = i;
            pool.execute(() -> {
                active.incrementAndGet();
                try {
                    downstreamDown.await();   // blocks ALL workers
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                active.decrementAndGet();
            });
        }

        Thread.sleep(300);
        System.out.println("Both workers are now blocked on the slow downstream.");
        System.out.println("A FAST task arrives (an unrelated quick request)...");

        // This "fast" task should complete in milliseconds, but the pool is exhausted.
        long fastStart = System.nanoTime();
        AtomicLong fastDoneAt = new AtomicLong();     // AtomicLong: safe cross-thread visibility
        pool.execute(() -> fastDoneAt.set(System.nanoTime()));
        Thread.sleep(200);

        System.out.println("  200ms later: fast task has NOT started yet -> it sits in the");
        System.out.println("  queue behind 4 other tasks (queue keeps growing).");
        System.out.println("  With an unbounded queue this piles up forever: memory grows,");
        System.out.println("  latency climbs - no exception is ever thrown.");
        System.out.println();

        // Downstream recovers -> latch opens -> everyone proceeds.
        downstreamDown.countDown();
        while (fastDoneAt.get() == 0) {
            Thread.sleep(10);               // wait until the fast task ran
        }
        long fastLatencyMs = (fastDoneAt.get() - fastStart) / 1_000_000;
        System.out.printf("After the downstream recovered, the 'fast' task ran. %n");
        System.out.printf("Total wait for the fast task: ~%d ms (it should have been < 1 ms).%n",
                fastLatencyMs);

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println();
        System.out.println("Observation:");
        System.out.println("When ALL workers block, even unrelated tasks wait for the queue");
        System.out.println("to drain. Production fixes: bounded queue + rejection policy");
        System.out.println("(backpressure), separate pools per workload type, timeouts and");
        System.out.println("circuit breakers on the slow downstream, and monitoring of");
        System.out.println("executor_queue_size (see BlockingSharedPoolExample).");
    }
}
