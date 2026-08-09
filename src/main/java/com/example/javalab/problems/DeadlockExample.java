package com.example.javalab.problems;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * A CONTROLLED deadlock between two threads and two locks.
 *
 * <p>Thread-1 acquires LOCK_A then LOCK_B; thread-2 acquires LOCK_B then
 * LOCK_A. Each holds one lock and waits for the other -> circular wait.
 *
 * <p>DANGEROUS, but controlled: both threads are DAEMON threads, so the JVM
 * can still exit after main() finishes. In a real application these threads
 * would block forever and the process would hang.
 *
 * <p>How to inspect a real deadlock:
 * <pre>
 *   jcmd &lt;pid&gt; Thread.print        (or jstack &lt;pid&gt;)
 *   -> prints "Found one Java-level deadlock" with the cycle
 * </pre>
 * While this example runs you can also attach jcmd and watch it live
 * (the main thread sleeps 3 seconds before shutting down).
 */
public class DeadlockExample {

    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Deadlock Example ===");
        System.out.println();
        System.out.println("Thread-1: lock A -> lock B");
        System.out.println("Thread-2: lock B -> lock A");
        System.out.println("Result: each holds one lock and waits for the other (circular wait).");
        System.out.println();

        Thread t1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("  [thread-1] holds LOCK_A, wants LOCK_B...");
                sleep(100);                       // make the interleaving deterministic
                synchronized (LOCK_B) {
                    System.out.println("  [thread-1] got LOCK_B (never printed)");
                }
            }
        }, "deadlock-thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (LOCK_B) {
                System.out.println("  [thread-2] holds LOCK_B, wants LOCK_A...");
                sleep(100);
                synchronized (LOCK_A) {
                    System.out.println("  [thread-2] got LOCK_A (never printed)");
                }
            }
        }, "deadlock-thread-2");

        // Daemon threads: the JVM may exit even if they are stuck.
        t1.setDaemon(true);
        t2.setDaemon(true);

        t1.start();
        t2.start();

        Thread.sleep(2_000);                      // give the deadlock time to form

        // The JVM itself can detect the deadlock programmatically.
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedIds = mxBean.findDeadlockedThreads();
        System.out.println();
        System.out.println("JVM deadlock detector (findDeadlockedThreads):");
        if (deadlockedIds == null) {
            System.out.println("  no deadlock detected (this should not happen)");
        } else {
            for (long id : deadlockedIds) {
                ThreadInfo info = mxBean.getThreadInfo(id);
                System.out.println("  DEADLOCKED: " + info.getThreadName()
                        + " state=" + info.getThreadState());
            }
        }

        System.out.println();
        System.out.println("TRY IT LIVE (optional): rerun with the main sleep at");
        System.out.println("   Thread.sleep(30_000) and inspect with:");
        System.out.println("   jcmd <pid> Thread.print   -> 'Found one Java-level deadlock'");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("In a REAL application these two threads would block forever,");
        System.out.println("CPU stays idle, latency climbs - and no exception is thrown.");
        System.out.println("Prevention: consistent lock ORDERING (always A then B),");
        System.out.println("tryLock(timeout) instead of lock(), and holding at most");
        System.out.println("one lock at a time.");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
