package com.example.javalab.synchronization;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ReentrantLock} - the programmatic sibling of {@code synchronized}.
 *
 * <p>What it adds over {@code synchronized}:
 * <ul>
 *   <li>{@code tryLock(timeout)} - give up after a deadline instead of
 *       blocking forever (deadlock defense).</li>
 *   <li>Fair mode - longest-waiting thread gets the lock first.</li>
 *   <li>{@link java.util.concurrent.locks.Condition} - precise wait/notify.</li>
 *   <li>{@code lockInterruptibly()} - interruption while waiting.</li>
 * </ul>
 *
 * <p>Demonstrates: a lock-protected counter, a successful {@code tryLock},
 * and a FAILED {@code tryLock} that would have blocked forever with
 * {@code synchronized}.
 */
public class LockExample {

    private final ReentrantLock lock = new ReentrantLock();
    private int count;

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();      // ALWAYS release in finally, or the lock leaks
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== ReentrantLock Example ===");
        System.out.println();

        // --- 1) Lock-protected counter ---
        LockExample counter = new LockExample();
        Thread t1 = new Thread(() -> repeat(counter, 50_000), "lock-worker-1");
        Thread t2 = new Thread(() -> repeat(counter, 50_000), "lock-worker-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.printf("1) Two threads x 50,000 increments: count = %d (expected 100000)%n%n",
                counter.count);

        // --- 2) tryLock that SUCCEEDS ---
        ReentrantLock l = new ReentrantLock();
        l.lock();
        try {
            System.out.println("2) main holds the lock, tries tryLock(1, SECONDS) again:");
            boolean acquired = l.tryLock(1, TimeUnit.SECONDS);
            System.out.println("   (reentrant, same thread) tryLock = " + acquired);
        } finally {
            l.unlock();
        }
        System.out.println();

        // --- 3) tryLock that FAILS (another thread holds the lock) ---
        ReentrantLock held = new ReentrantLock();
        Thread holder = new Thread(() -> {
            held.lock();
            try {
                Thread.sleep(3_000);          // hold the lock for 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                held.unlock();
            }
        }, "lock-holder");
        holder.start();
        Thread.sleep(200);                    // let the holder take the lock

        System.out.println("3) another thread holds the lock for 3s; main calls tryLock(1, SECONDS):");
        boolean acquired = held.tryLock(1, TimeUnit.SECONDS);
        System.out.println("   tryLock = " + acquired
                + " (main did NOT wait for the holder - it moved on)");
        System.out.println("   With synchronized this would have blocked until released.");
        holder.join();

        System.out.println();
        System.out.println("Observation:");
        System.out.println("tryLock(timeout) is the first defense against deadlocks and");
        System.out.println("indefinite blocking. Prefer it over lock() whenever waiting");
        System.out.println("forever is unacceptable.");
    }

    private static void repeat(LockExample counter, int times) {
        for (int i = 0; i < times; i++) {
            counter.increment();
        }
    }
}
