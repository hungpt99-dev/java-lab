package com.example.javalab.problems;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The ThreadLocal + thread pool trap.
 *
 * <p>Plain threads die after run(), taking their ThreadLocal values with them.
 * Pool threads live for YEARS. A ThreadLocal value that is never removed:
 * <ol>
 *   <li>leaks memory (the thread keeps the reference forever), and</li>
 *   <li>leaks DATA: the next task that reuses that thread sees the PREVIOUS
 *       task's value - e.g. one user's security context in another user's request.</li>
 * </ol>
 *
 * <p>Phase 1 runs the broken version, Phase 2 the fix
 * ({@code remove()} in a {@code finally} block).
 */
public class ThreadLocalLeakExample {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        System.out.println("=== ThreadLocal Leak Example ===");
        System.out.println();
        System.out.println("Pool of 2 threads. Each task sets CURRENT_USER for 'its' user.");
        System.out.println();

        System.out.println("PHASE 1 - BROKEN: tasks never call remove()");
        System.out.println("---------------------------------------------");
        runPhase(false);
        System.out.println();

        System.out.println("PHASE 2 - FIXED: tasks call remove() in finally");
        System.out.println("---------------------------------------------");
        runPhase(true);

        System.out.println();
        System.out.println("Observation:");
        System.out.println("Phase 1: readers see STALE values left by earlier tasks, and the");
        System.out.println("2 pool threads keep those Strings referenced forever (memory leak).");
        System.out.println("In real apps this also means: one request seeing ANOTHER request's");
        System.out.println("security context - a data leak, not just a memory leak.");
        System.out.println("Phase 2: remove() in finally - no leak, no stale data.");
    }

    /** Runs one phase: 6 setters, then 6 readers, on a fresh 2-thread pool. */
    private static void runPhase(boolean useRemove) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2, runnable ->
                new Thread(runnable, useRemove ? "clean-worker" : "leaky-worker"));

        // 1) Setters: each task claims a "user" and stores it in the ThreadLocal.
        CountDownLatch settersDone = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            final String user = "user-" + ((i % 3) + 1);
            pool.execute(() -> {
                try {
                    CURRENT_USER.set(user);
                    sleep(50);
                } finally {
                    if (useRemove) {
                        CURRENT_USER.remove();      // the fix
                    }
                    settersDone.countDown();
                }
            });
        }
        settersDone.await(10, TimeUnit.SECONDS);

        // 2) Readers: same pool, same 2 threads - what do they see?
        CountDownLatch readersDone = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            final int reader = i;
            pool.execute(() -> {
                String seen = CURRENT_USER.get();
                if (useRemove) {
                    System.out.println("  reader-" + reader + " sees user=" + seen
                            + "  <-- clean (nothing leaked between tasks)");
                } else {
                    System.out.println("  reader-" + reader + " sees user=" + seen
                            + " on " + Thread.currentThread().getName()
                            + "  <-- STALE value set by an EARLIER task!");
                }
                readersDone.countDown();
            });
        }
        readersDone.await(10, TimeUnit.SECONDS);

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
