package com.example.javalab.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controlled experiment: the SAME workload run with different numbers of
 * platform threads - few, reasonable, and excessive.
 *
 * <p>Configuration via command line:
 * <pre>
 *   java ... TooManyThreadsExample [tasks] [threadsToTest...]
 *   default: tasks=400, thread counts = 4, 64, 400, 800
 * </pre>
 *
 * <p>Each task does ~10 ms of mixed work (compute + simulated wait). Results
 * are machine-specific - this is a demonstration, not a universal benchmark.
 */
public class TooManyThreadsExample {

    public static void main(String[] args) throws Exception {
        int tasks = args.length > 0 ? Integer.parseInt(args[0]) : 400;
        int[] threadCounts = args.length > 1
                ? parseThreadCounts(args)
                : new int[]{4, 64, 400, 800};

        System.out.println("=== Too Many Threads Example ===");
        System.out.println();
        System.out.println("Tasks: " + tasks + " (each ~10 ms of mixed work)");
        System.out.println("CPU cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Memory: each platform thread reserves ~1 MB of stack");
        System.out.println();
        System.out.println("This is NOT a universal benchmark - numbers depend on the");
        System.out.println("machine, the JIT and the workload. Watch the TREND.");
        System.out.println();

        // Warm up the JIT.
        runWorkload(4, 10);
        System.out.println("(JIT warmup done)");
        System.out.println();

        System.out.printf("%-12s %-10s %-14s%n", "threads", "tasks", "wall time (ms)");
        System.out.printf("%-12s %-10s %-14s%n", "-------", "-----", "--------------");
        for (int threads : threadCounts) {
            long wallMs = runWorkload(threads, tasks);
            System.out.printf("%-12d %-10d %-14d%n", threads, tasks, wallMs);
        }

        System.out.println();
        System.out.println("What to look for:");
        System.out.println("  - Increasing threads first HELPS (more parallelism).");
        System.out.println("  - Past a point (around #cores) gains flatten out.");
        System.out.println("  - With excessive threads the time can go UP again:");
        System.out.println("    context switching + cache thrashing eat the CPU.");
        System.out.println("  - Creating 10,000+ platform threads can also fail with");
        System.out.println("    OutOfMemoryError: unable to create native thread.");
        System.out.println();
        System.out.println("Key lesson: thread count must match the workload type and");
        System.out.println("the available cores. Bigger is NOT better.");
    }

    private static long runWorkload(int threads, int tasks) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(tasks);

        long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            pool.execute(() -> {
                doMixedWork();
                done.countDown();
            });
        }
        done.await(120, TimeUnit.SECONDS);
        long wallMs = (System.nanoTime() - start) / 1_000_000;

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return wallMs;
    }

    /** ~10 ms of mixed work: a bit of compute and a bit of simulated wait. */
    private static void doMixedWork() {
        long deadline = System.nanoTime() + 5_000_000;   // 5 ms compute
        int acc = 0;
        while (System.nanoTime() < deadline) {
            acc += (acc * 31 + 7) & 0x7FFF;
        }
        try {
            Thread.sleep(5);                             // 5 ms simulated I/O
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (acc == -1) {                                 // never true; defeats dead-code removal
            System.out.print(acc);
        }
    }

    private static int[] parseThreadCounts(String[] args) {
        int[] counts = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            counts[i - 1] = Integer.parseInt(args[i]);
        }
        return counts;
    }
}
