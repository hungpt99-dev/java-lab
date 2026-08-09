package com.example.javalab.basics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Compares {@link Runnable} and {@link Callable}:
 * <ul>
 *   <li>{@code Runnable}: {@code void run()}, no result, cannot throw checked exceptions.</li>
 *   <li>{@code Callable}: {@code V call()}, returns a result, may throw checked exceptions.</li>
 * </ul>
 *
 * <p>Also demonstrates {@code Thread.sleep}, {@code join}, thread names and
 * how a {@code Callable} result is retrieved through a {@link Future}.
 */
public class RunnableExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Runnable vs Callable Example ===");
        System.out.println();

        // --- Runnable: no return value ---
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            System.out.println("[" + name + "] start");
            try {
                Thread.sleep(100);              // simulated work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();   // restore the interrupt flag
            }
            System.out.println("[" + name + "] end");
        };

        Thread t = new Thread(task, "runnable-worker");
        t.start();
        t.join();                                // wait for the worker to finish
        System.out.println("main: joined " + t.getName() + ", final state = " + t.getState());
        System.out.println();

        // --- Callable: returns a result ---
        Callable<Integer> callable = () -> {
            Thread.sleep(100);
            return 42;                           // Callable produces a value
        };

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> future = pool.submit(callable);
            System.out.println("Callable result: " + future.get());   // blocks until ready
        } finally {
            pool.shutdown();                     // ALWAYS shut down executors
        }

        System.out.println();
        System.out.println("Observation:");
        System.out.println("- Runnable.run()   -> void;        cannot throw checked exceptions.");
        System.out.println("- Callable.call()  -> V;           can throw checked exceptions.");
        System.out.println("- Future.get()     -> V;           blocks and returns the result");
        System.out.println("  (or throws ExecutionException if the task failed).");
    }
}
