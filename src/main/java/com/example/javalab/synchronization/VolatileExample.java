package com.example.javalab.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Two separate demonstrations around {@code volatile}:
 *
 * <p><b>Part A - volatile does NOT make {@code count++} atomic.</b>
 * {@code volatile} only guarantees visibility and ordering, never atomicity.
 * The read-modify-write sequence can still interleave between threads, so
 * increments are still lost even on a {@code volatile int}.
 *
 * <p><b>Part B - visibility of a flag.</b> Without {@code volatile}, a worker
 * thread may keep running long after another thread changed the flag, because
 * the JIT can cache the field (register/CPU cache) and never re-read it.
 * {@code volatile} forces every read to see the latest write. This part is
 * nondeterministic - whether the bug shows in a given trial depends on the JIT.
 */
public class VolatileExample {

    // Part A state
    private volatile int count;              // volatile: visible, but STILL not atomic

    // Part B state
    private boolean keepRunning = true;      // NOT volatile -> visibility risk
    private volatile boolean forceStop = false;   // escape hatch that always works

    public void increment() {
        count++;                             // still READ+ADD+WRITE: racy despite volatile
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== volatile Example ===");
        System.out.println();

        partA_volatileIsNotAtomic();
        partB_visibility();

        System.out.println();
        System.out.println("Takeaway:");
        System.out.println("volatile  = visibility + ordering only.");
        System.out.println("synchronized / AtomicInteger / locks = atomicity + visibility.");
        System.out.println("Rule of thumb: volatile for flags and status,");
        System.out.println("AtomicInteger/AtomicLong for counters and shared state.");
    }

    /** Part A: increments are lost even though the field is volatile. */
    private static void partA_volatileIsNotAtomic() throws Exception {
        System.out.println("Part A - volatile int count++; does it stay atomic?");
        System.out.println();

        VolatileExample counter = new VolatileExample();
        int threadCount = 8;
        int incrementsPerThread = 50_000;

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(pool.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            }));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        int expected = threadCount * incrementsPerThread;
        System.out.printf("expected=%d actual=%d %s%n%n",
                expected, counter.count,
                counter.count == expected ? "(correct this time)" : "(<-- WRONG)");
        System.out.println("volatile made the field VISIBLE to all threads, but the");
        System.out.println("three-step READ+ADD+WRITE can still interleave. Atomicity");
        System.out.println("needs AtomicInteger (CAS) or synchronized.");
    }

    /** Part B: a non-volatile flag may never be observed by the worker. */
    private static void partB_visibility() throws Exception {
        System.out.println("Part B - visibility of a shared flag (keepRunning, NOT volatile)");
        System.out.println();

        int trials = 3;
        int observedProblems = 0;

        for (int trial = 1; trial <= trials; trial++) {
            VolatileExample demo = new VolatileExample();

            Thread worker = new Thread(() -> {
                long iterations = 0;
                // Tight loop: the JIT may hoist keepRunning out of the loop,
                // so the change made by main may never be seen.
                while (demo.keepRunning && !demo.forceStop) {
                    iterations++;
                }
                System.out.println("  worker stopped after " + iterations + " iterations");
            }, "worker-" + trial);

            worker.start();
            Thread.sleep(200);          // let the worker warm up (JIT compiles the loop)
            demo.keepRunning = false;   // try to stop the worker from main

            worker.join(1_500);         // give it 1.5 s to notice
            if (worker.isAlive()) {
                observedProblems++;
                System.out.println("  -> PROBLEM: worker did not see keepRunning=false for 1.5s!");
                System.out.println("     (the write was never made visible; JIT cached the field)");
                demo.forceStop = true;  // escape hatch: a VOLATILE flag always works
                worker.join();
                System.out.println("     set forceStop (volatile) -> worker stopped immediately");
            } else {
                System.out.println("  -> this run happened to observe the write (nondeterministic)");
            }
        }

        System.out.printf("%nVisibility problem reproduced in %d/%d trials.%n",
                observedProblems, trials);
        System.out.println("This is exactly why shared flags should be volatile (or");
        System.out.println("guarded by synchronized): writes are not automatically");
        System.out.println("visible to other threads.");
    }
}
