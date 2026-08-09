package com.example.javalab.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CPU-bound workload benchmark: naive prime counting.
 *
 * <p>Question: does throwing more threads at CPU-heavy work make it faster?
 * Answer: only up to the number of cores. Beyond that, extra threads add
 * context-switching overhead and the wall time gets WORSE.
 *
 * <p>Results are machine-specific - tune {@code PRIME_LIMIT} and
 * {@code TASKS} so a single task takes a measurable amount of time.
 */
public class CpuBoundThreadExample {

    /** Work size: higher = longer per task. ~100-300 ms per task on a modern CPU. */
    private static final int PRIME_LIMIT = 150_000;

    /** Total tasks: keep it constant across all pool sizes. */
    private static final int TASKS = 32;

    public static void main(String[] args) throws Exception {
        System.out.println("=== CPU-bound Workload: Thread Count vs Wall Time ===");
        System.out.println();
        System.out.println("Workload: count primes up to " + PRIME_LIMIT + " (" + TASKS
                + " tasks, fixed total work)");
        System.out.println("CPU cores available: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        // Warm up the JIT so the first measurement is not distorted.
        cpuIntensiveWork();
        System.out.println("(JIT warmup done)");
        System.out.println();

        System.out.printf("%-22s %-10s %-12s %-10s%n", "threads", "tasks", "wall time", "note");
        System.out.printf("%-22s %-10s %-12s %-10s%n", "-------", "-----", "---------", "----");

        int cores = Runtime.getRuntime().availableProcessors();
        int[] poolSizes = {1, cores, cores * 4, cores * 16};

        for (int poolSize : poolSizes) {
            long wallMs = runWorkload(poolSize, TASKS);
            String note;
            if (poolSize == 1) {
                note = "baseline";
            } else if (poolSize <= cores) {
                note = "up to cores: helps";
            } else if (poolSize == cores * 4) {
                note = "beyond cores";
            } else {
                note = "excessive";
            }
            System.out.printf("%-22d %-10d %-12d %-10s%n", poolSize, TASKS, wallMs, note);
        }

        System.out.println();
        System.out.println("Observation:");
        System.out.println("Compare the wall times in the table above:");
        System.out.println("- Going from 1 thread to #cores threads usually gives a");
        System.out.println("  near-linear speedup (that is parallelism in action).");
        System.out.println("- Beyond #cores the gains flatten out; with enough threads");
        System.out.println("  the time can creep back up because of context switching.");
        System.out.println();
        System.out.println("Key lesson: CPU-bound work is bounded by CORES, not threads.");
        System.out.println("Adding threads beyond the core count buys context switches,");
        System.out.println("not CPU power. Optimal pool size for CPU-bound work: ~#cores");
        System.out.println("(your exact numbers depend on machine, JIT and work size).");
    }

    /** Runs the fixed workload on a pool of the given size; returns wall time in ms. */
    private static long runWorkload(int poolSize, int tasks) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        CountDownLatch done = new CountDownLatch(tasks);

        long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            pool.execute(() -> {
                cpuIntensiveWork();
                done.countDown();
            });
        }
        done.await(120, TimeUnit.SECONDS);
        long wallMs = (System.nanoTime() - start) / 1_000_000;

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return wallMs;
    }

    /** Deterministic CPU-heavy work: count primes with the naive O(n*sqrt(n)) method. */
    private static long cpuIntensiveWork() {
        long primeCount = 0;
        for (int n = 2; n <= PRIME_LIMIT; n++) {
            boolean prime = true;
            for (int d = 2; (long) d * d <= n; d++) {
                if (n % d == 0) {
                    prime = false;
                    break;
                }
            }
            if (prime) {
                primeCount++;
            }
        }
        return primeCount;
    }
}
