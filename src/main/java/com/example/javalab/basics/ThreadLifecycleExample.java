package com.example.javalab.basics;

/**
 * Walks a single thread through all six lifecycle states:
 * NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
 *
 * <p>States are sampled from the main thread via {@link Thread#getState()}.
 * Exact timing is nondeterministic, so the example polls until each expected
 * state is observed (with a timeout) instead of relying on sleep durations.
 */
public class ThreadLifecycleExample {

    /** Shared lock used to manufacture the BLOCKED and WAITING states. */
    private static final Object LOCK = new Object();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Thread Lifecycle Example ===");
        System.out.println();
        System.out.println("State machine: NEW -> RUNNABLE -> BLOCKED/WAITING/TIMED_WAITING -> TERMINATED");
        System.out.println();

        // --- Thread 1: demonstrates BLOCKED -------------------------------
        Thread blocked = new Thread(() -> {
            synchronized (LOCK) {                       // blocks until main releases LOCK
                System.out.println("  [blocked-worker] acquired the lock, done.");
            }
        }, "blocked-worker");

        Thread worker = new Thread(() -> {
            try {
                // Busy-spin briefly so the main thread can observe RUNNABLE.
                long deadline = System.nanoTime() + 100_000_000;   // 100 ms
                while (System.nanoTime() < deadline) {
                    // spin
                }

                synchronized (LOCK) {
                    LOCK.wait(300);                     // TIMED_WAITING for 300 ms
                    LOCK.wait();                        // WAITING until notified
                    System.out.println("  [lifecycle-worker] woken up, finishing.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "lifecycle-worker");

        // 1) NEW: constructed, not started yet.
        System.out.println("1) NEW         state = " + worker.getState());
        System.out.println("   (not even an OS thread yet)");
        worker.start();

        // 2) RUNNABLE: running (or ready to run).
        awaitState(worker, Thread.State.RUNNABLE);
        System.out.println("2) RUNNABLE    state = " + worker.getState()
                + "  (running or ready - Java does not distinguish)");

        // 3) BLOCKED: waiting for a monitor held by another thread (main).
        synchronized (LOCK) {                           // main takes the lock
            blocked.start();                            // will block on LOCK
            awaitState(blocked, Thread.State.BLOCKED);
            System.out.println("3) BLOCKED     state = " + blocked.getState()
                    + "  (waiting for synchronized monitor held by main)");
        }                                               // main releases the lock

        // 4) TIMED_WAITING: sleeping / waiting with a deadline.
        awaitState(worker, Thread.State.TIMED_WAITING);
        System.out.println("4) TIMED_WAITING state = " + worker.getState()
                + "  (LOCK.wait(300) / Thread.sleep)");

        // 5) WAITING: parked indefinitely until notified.
        awaitState(worker, Thread.State.WAITING);
        System.out.println("5) WAITING     state = " + worker.getState()
                + "  (LOCK.wait() - parked until notify)");

        // Wake the worker, then observe TERMINATED.
        synchronized (LOCK) {
            LOCK.notifyAll();
        }

        // 6) TERMINATED: run() returned.
        worker.join();
        blocked.join();
        System.out.println("6) TERMINATED  state = " + worker.getState());
        System.out.println("   state = " + blocked.getState());

        System.out.println();
        System.out.println("Where you see these states in production:");
        System.out.println("- BLOCKED piles     -> synchronized contention on a hot object");
        System.out.println("- WAITING/TIMED     -> parked on futures, pool queues, sleep");
        System.out.println("- RUNNABLE floods   -> machine is CPU saturated");
        System.out.println("- A thread dump (jcmd <pid> Thread.print) shows these states.");
    }

    /** Polls until the thread reaches the expected state, with a timeout. */
    private static void awaitState(Thread t, Thread.State expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000;
        while (t.getState() != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
        if (t.getState() != expected) {
            System.out.println("   [warn] did not observe " + expected + " in time, current=" + t.getState());
        }
    }
}
