package com.example.javalab.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A real {@link ThreadPoolExecutor} with ALL knobs configured, demonstrating
 * the exact task-acceptance pipeline:
 *
 * <pre>
 *   task --(1)--> core threads --(2)--> bounded queue --(3)--> max threads --(4)--> rejection
 * </pre>
 *
 * <p>Configuration used here: core=2, max=4, queue capacity=2.
 * Submission order determines the path every task takes:
 *
 * <pre>
 *   task 1,2 -> run on the 2 core threads
 *   task 3,4 -> wait in the queue
 *   task 5,6 -> spawn 2 extra threads (up to max=4) because the queue is full
 *   task 7,8,9 -> queue full AND max reached -> RejectedExecutionException
 * </pre>
 */
public class ThreadPoolExecutorExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== ThreadPoolExecutor Example ===");
        System.out.println();
        System.out.println("core=2  max=4  queue capacity=2  (AbortPolicy)");
        System.out.println();
        System.out.println("Pipeline:");
        System.out.println("  task -> core threads -> bounded queue -> max threads -> rejection");
        System.out.println();

        AtomicInteger threadCounter = new AtomicInteger(1);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                                    // corePoolSize
                4,                                    // maximumPoolSize
                30, TimeUnit.SECONDS,                 // keepAliveTime for threads above core
                new ArrayBlockingQueue<>(2),          // BOUNDED work queue
                runnable -> new Thread(runnable, "pool-thread-" + threadCounter.getAndIncrement()),
                new ThreadPoolExecutor.AbortPolicy()); // reject with exception when full

        int rejected = 0;
        for (int i = 1; i <= 9; i++) {
            final int taskId = i;
            try {
                executor.submit(() -> {
                    System.out.println("    task " + taskId + " START  on "
                            + Thread.currentThread().getName());
                    try {
                        Thread.sleep(1_500);          // slow work keeps the pool busy
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("    task " + taskId + " END    on "
                            + Thread.currentThread().getName());
                });
                System.out.printf("submitted %d -> poolSize=%d queueSize=%d%n",
                        i, executor.getPoolSize(), executor.getQueue().size());
            } catch (RejectedExecutionException e) {
                rejected++;
                System.out.printf("submitted %d -> REJECTED (pool full, queue full): %s%n",
                        i, e.getClass().getSimpleName());
            }
            Thread.sleep(10);                         // let the pool react between submits
        }

        System.out.println();
        System.out.println("Results:");
        System.out.println("  accepted  = 9 - " + rejected);
        System.out.println("  rejected  = " + rejected);
        System.out.println("  max pool size reached = " + executor.getLargestPoolSize()
                + " (expected 4)");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("The queue is used BEFORE the pool grows: tasks 5 and 6 only");
        System.out.println("spawned new threads because the queue was already full.");
        System.out.println("With an unbounded queue, maximumPoolSize would NEVER be reached.");
        System.out.println("AbortPolicy throws - in production prefer CallerRunsPolicy");
        System.out.println("for backpressure (see RejectedExecutionExample).");

        executor.shutdownNow();                       // cleanup: stop and interrupt workers
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
