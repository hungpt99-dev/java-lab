package com.example.javalab.problems;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The shared-pool disaster: blocking work mixed with fast work in ONE pool.
 *
 * <p>BAD design: a single pool of 4 threads serves both "slow external call"
 * tasks and "fast local" tasks. When 4 slow tasks block, fast tasks queue up
 * behind them and their latency explodes - one slow downstream service takes
 * down unrelated functionality.
 *
 * <p>GOOD design: separate pools per workload type. Slow calls get their own
 * pool, fast work gets its own pool, and fast tasks stay fast.
 */
public class BlockingSharedPoolExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Blocking Shared Thread Pool Example ===");
        System.out.println();
        System.out.println("Pool of 4 threads. 4 slow tasks (2 s each, simulated");
        System.out.println("downstream call) + 4 fast tasks (5 ms each).");
        System.out.println();

        // ================= BAD DESIGN: one shared pool =================
        System.out.println("BAD DESIGN - one shared pool for everything:");
        ExecutorService shared = Executors.newFixedThreadPool(4, runnable ->
                new Thread(runnable, "shared-worker"));

        for (int i = 1; i <= 4; i++) {
            shared.execute(() -> slowDownstreamCall());
        }
        Thread.sleep(100);                     // let the slow tasks grab all 4 workers

        CountDownLatch fastDoneBad = new CountDownLatch(4);
        long[] badLatencies = new long[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            long start = System.nanoTime();
            shared.execute(() -> {
                fastLocalWork();
                badLatencies[idx] = (System.nanoTime() - start) / 1_000_000;
                fastDoneBad.countDown();
            });
        }
        fastDoneBad.await(15, TimeUnit.SECONDS);
        System.out.printf("   fast task latencies: %s ms%n", format(badLatencies));

        shared.shutdownNow();
        shared.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println();

        // ================= GOOD DESIGN: separate pools =================
        System.out.println("GOOD DESIGN - dedicated pools per workload type:");
        ExecutorService slowPool = Executors.newFixedThreadPool(4, runnable ->
                new Thread(runnable, "slow-call-worker"));
        ExecutorService fastPool = Executors.newFixedThreadPool(4, runnable ->
                new Thread(runnable, "fast-work-worker"));

        for (int i = 1; i <= 4; i++) {
            slowPool.execute(() -> slowDownstreamCall());
        }
        Thread.sleep(100);

        CountDownLatch fastDoneGood = new CountDownLatch(4);
        long[] goodLatencies = new long[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            long start = System.nanoTime();
            fastPool.execute(() -> {
                fastLocalWork();
                goodLatencies[idx] = (System.nanoTime() - start) / 1_000_000;
                fastDoneGood.countDown();
            });
        }
        fastDoneGood.await(15, TimeUnit.SECONDS);
        System.out.printf("   fast task latencies: %s ms%n", format(goodLatencies));

        slowPool.shutdownNow();
        fastPool.shutdownNow();
        slowPool.awaitTermination(5, TimeUnit.SECONDS);
        fastPool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println();
        System.out.println("Observation:");
        System.out.println("BAD:  slow calls block all workers -> fast tasks wait ~2 s.");
        System.out.println("GOOD: fast tasks stay ~5 ms - slow work no longer affects them.");
        System.out.println("Production rule: size each pool for ITS workload, never mix");
        System.out.println("blocking calls and latency-critical work in one pool.");
    }

    private static void slowDownstreamCall() {
        try {
            Thread.sleep(2_000);               // simulated slow external API / DB
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void fastLocalWork() {
        try {
            Thread.sleep(5);                   // simulated fast local computation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String format(long[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values[i]);
        }
        return sb.append("]").toString();
    }
}
