package com.example.javalab.problems;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exceptions that vanish into thin air.
 *
 * <p>{@code execute(Runnable)}  -> exceptions go to the thread's
 * UncaughtExceptionHandler (by default: printed to stderr).
 *
 * <p>{@code submit(Runnable)}   -> exceptions are CAPTURED inside the returned
 * {@link Future}. If nobody ever calls {@code future.get()}, the failure is
 * swallowed silently: no log, no metric, no error - the system looks healthy.
 *
 * <p>This is one of the main reasons concurrency bugs "only appear in
 * production": the errors are produced, caught by the machinery, and hidden.
 */
public class LostExceptionExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Lost Exceptions Example ===");
        System.out.println();

        // --- 1) submit() + ignored Future: the exception DISAPPEARS ---
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            throw new IllegalStateException("boom (submitted, result never inspected)");
        });
        Thread.sleep(300);
        System.out.println("1) submit() and NEVER call future.get():");
        System.out.println("   ...the task threw, but nothing printed, nothing logged.");
        System.out.println("   The exception sits inside the Future - invisible.");
        System.out.println();
        // NOTE: this pool is shut down right after the demonstration. Forgetting to
        // shut down an executor leaks a NON-DAEMON worker thread that keeps the JVM
        // alive forever - the exact bug this example is about, in its own code.
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // --- 2) execute(): the exception reaches the UncaughtExceptionHandler ---
        AtomicInteger caught = new AtomicInteger();
        ExecutorService pool2 = Executors.newSingleThreadExecutor(runnable -> {
            Thread t = new Thread(runnable, "visible-worker");
            t.setUncaughtExceptionHandler((thread, error) -> {
                caught.incrementAndGet();
                System.out.println("2) execute() -> UncaughtExceptionHandler caught: "
                        + error.getMessage());
            });
            return t;
        });
        pool2.execute(() -> {
            throw new IllegalStateException("kaboom (via execute)");
        });
        Thread.sleep(300);
        System.out.println();
        pool2.shutdown();
        pool2.awaitTermination(5, TimeUnit.SECONDS);

        // --- 3) submit() + get(): the exception is surfaced ---
        ExecutorService pool3 = Executors.newSingleThreadExecutor();
        Future<?> future = pool3.submit(() -> {
            throw new IllegalStateException("kapow (via submit + get)");
        });
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            System.out.println("3) submit() + future.get() surfaced the failure:");
            System.out.println("   ExecutionException cause = "
                    + e.getCause().getMessage());
        }
        pool3.shutdown();
        pool3.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println();
        System.out.println("Observation:");
        System.out.println("- execute(): exception escapes to the UncaughtExceptionHandler.");
        System.out.println("- submit() : exception is stored in the Future. Ignore the");
        System.out.println("  Future and the failure is GONE - silent failure in production.");
        System.out.println("- Fixes: always handle futures, or wrap task bodies in");
        System.out.println("  try/catch and log (never silently swallow).");
    }
}
