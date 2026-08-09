package com.example.javalab.virtualthread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The honest truth: virtual threads do NOT speed up CPU-bound work.
 *
 * <p>CPU-bound work is bounded by the number of cores. Running it on 10,000
 * virtual threads gives you the same wall time as running it on a pool of
 * #cores platform threads - plus a little scheduling overhead.
 *
 * <p>Compare with CpuBoundThreadExample in the {@code performance} package.
 */
public class VirtualThreadCpuBoundExample {

    private static final int PRIME_LIMIT = 150_000;
    private static final int TASKS = 16;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads and CPU-bound Work ===");
        System.out.println();
        System.out.println("Workload: " + TASKS + " x count primes up to " + PRIME_LIMIT);
        System.out.println("CPU cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        cpuIntensiveWork();   // JIT warmup
        System.out.println("(JIT warmup done)");
        System.out.println();

        int cores = Runtime.getRuntime().availableProcessors();

        long fixedTime = runOnPool(cores, TASKS);
        long fixed4xTime = runOnPool(cores * 4, TASKS);
        long virtualTime = runOnVirtualThreads(TASKS);

        System.out.printf("  platform pool %3d threads: %5d ms%n", cores, fixedTime);
        System.out.printf("  platform pool %3d threads: %5d ms%n", cores * 4, fixed4xTime);
        System.out.printf("  virtual threads          : %5d ms%n", virtualTime);

        System.out.println();
        System.out.println("Observation:");
        System.out.println("Virtual threads run the SAME CPU work at roughly the SAME");
        System.out.println("speed as a correctly sized platform pool - no magic speedup,");
        System.out.println("sometimes a hair slower due to scheduling overhead.");
        System.out.println("Rule: CPU-bound -> use a fixed pool of ~#cores.");
        System.out.println("       I/O-bound  -> virtual threads shine.");
    }

    private static long runOnPool(int poolSize, int tasks) throws InterruptedException {
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
        long ms = (System.nanoTime() - start) / 1_000_000;
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return ms;
    }

    private static long runOnVirtualThreads(int tasks) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(tasks);
        long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            Thread.startVirtualThread(() -> {
                cpuIntensiveWork();
                done.countDown();
            });
        }
        done.await(120, TimeUnit.SECONDS);
        return (System.nanoTime() - start) / 1_000_000;
    }

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
