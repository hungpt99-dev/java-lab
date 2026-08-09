package com.example.javalab.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * I/O-bound workload simulation: each task "waits" 40 ms (simulated network /
 * database latency, implemented with {@link LockSupport#parkNanos} so no
 * InterruptedException handling is needed) plus ~1 ms of real compute.
 *
 * <p>Contrast with CpuBoundThreadExample: here MORE threads DO help, because
 * waiting tasks consume no CPU - the CPU is free to run other tasks while one
 * task waits. Throughput scales with concurrency until every core is busy.
 */
public class IoBoundThreadExample {

    private static final long IO_WAIT_NANOS = 40_000_000;   // 40 ms "remote call"
    private static final int COMPUTE_MS = 1;                 // 1 ms local work
    private static final int TASKS = 120;                    // total tasks, fixed

    public static void main(String[] args) throws Exception {
        System.out.println("=== I/O-bound Workload: Concurrency vs Throughput ===");
        System.out.println();
        System.out.println("Each task: 40 ms simulated I/O wait + 1 ms compute.");
        System.out.println("Total tasks: " + TASKS + " (constant). " + "CPU cores: "
                + Runtime.getRuntime().availableProcessors());
        System.out.println();
        System.out.println("Classic sizing rule: threads = cores * (1 + wait / compute)");
        System.out.println("Here wait/compute = 40 -> ~41 threads per core");
        System.out.println();

        System.out.printf("%-10s %-10s %-12s %-14s%n", "threads", "tasks", "wall time", "throughput");
        System.out.printf("%-10s %-10s %-12s %-14s%n", "-------", "-----", "---------", "----------");

        int cores = Runtime.getRuntime().availableProcessors();
        int[] concurrency = {1, cores, cores * 8, TASKS};

        for (int poolSize : concurrency) {
            long wallMs = runWorkload(poolSize, TASKS);
            double throughput = TASKS * 1000.0 / wallMs;   // tasks per second
            System.out.printf("%-10d %-10d %-12d %-14.0f%n",
                    poolSize, TASKS, wallMs, throughput);
        }

        System.out.println();
        System.out.println("Observation:");
        System.out.println("Compare the wall times in the table above:");
        System.out.println("- Going from 1 thread to higher concurrency reduces wall time");
        System.out.println("  dramatically: while one task waits, others run.");
        System.out.println("- Throughput keeps climbing until every core is busy or the");
        System.out.println("  simulated wait becomes the only cost left.");
        System.out.println("- Beyond that point more threads add overhead - and in real");
        System.out.println("  systems the LIMIT is whatever the tasks wait on (DB");
        System.out.println("  connections, API quotas), not the thread count.");
        System.out.println();
        System.out.println("Key lesson: I/O-bound work benefits from HIGHER concurrency");
        System.out.println("because blocked threads cost ~nothing (no CPU while waiting).");
        System.out.println("The formula tells you where to start; the real limit is the");
        System.out.println("resource everyone waits on (DB connections, API quotas).");
        System.out.println("Your exact numbers depend on machine and JIT state.");
    }

    private static long runWorkload(int poolSize, int tasks) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        CountDownLatch done = new CountDownLatch(tasks);

        long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            pool.execute(() -> {
                LockSupport.parkNanos(IO_WAIT_NANOS);   // simulated blocking I/O
                for (int j = 0; j < COMPUTE_MS * 1_000_000 / 2; j++) {
                    // small deterministic compute (~1 ms)
                    int x = j * 31;
                    if ((x & 0xF) == 0) {
                        Thread.onSpinWait();
                    }
                }
                done.countDown();
            });
        }
        done.await(120, TimeUnit.SECONDS);
        long wallMs = (System.nanoTime() - start) / 1_000_000;

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return wallMs;
    }
}
