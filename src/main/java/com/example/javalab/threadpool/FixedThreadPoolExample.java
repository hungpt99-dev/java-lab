package com.example.javalab.threadpool;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The classic {@code Executors.newFixedThreadPool(n)}.
 *
 * <p>Important fact: a fixed pool is internally a
 * {@link java.util.concurrent.ThreadPoolExecutor} whose work queue is an
 * UNBOUNDED {@link java.util.concurrent.LinkedBlockingQueue}. Because the
 * queue never fills up, the pool NEVER grows beyond its core size - that is
 * why it is "fixed".
 */
public class FixedThreadPoolExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Fixed Thread Pool Example ===");
        System.out.println();
        System.out.println("3 threads execute 10 tasks; threads are REUSED.");
        System.out.println();

        // Named threads make logs readable in production.
        AtomicInteger threadCounter = new AtomicInteger(1);
        ThreadFactory namedFactory = runnable ->
                new Thread(runnable, "fixed-worker-" + threadCounter.getAndIncrement());

        ExecutorService pool = Executors.newFixedThreadPool(3, namedFactory);

        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            pool.submit(() -> {
                System.out.println("  task " + taskId + " running on "
                        + Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        pool.shutdown();                        // stop accepting new tasks
        boolean finished = pool.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println();
        System.out.println("All tasks finished: " + finished);
        System.out.println("The 3 worker threads handled all 10 tasks - workers were reused.");

        System.out.println();
        System.out.println("Inside the box:");
        System.out.println("newFixedThreadPool(3) == ThreadPoolExecutor(3, 3, 0L,");
        System.out.println("    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>())");
        System.out.println("Because the queue is UNBOUNDED, the pool can never grow");
        System.out.println("beyond 3 threads and can never reject a task - tasks just");
        System.out.println("pile up in memory. Use BoundedQueueExample to see the fix.");
    }
}
