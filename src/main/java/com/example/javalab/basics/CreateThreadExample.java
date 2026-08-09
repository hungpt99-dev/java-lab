package com.example.javalab.basics;

/**
 * Demonstrates the three ways to create and run a {@link Thread}:
 * <ol>
 *   <li>{@code Thread} with an anonymous {@link Runnable}</li>
 *   <li>{@code Thread} with a lambda {@link Runnable}</li>
 *   <li>a {@code Thread} subclass</li>
 * </ol>
 *
 * <p>Key takeaways:
 * <ul>
 *   <li>A thread is created with {@code new}, but it only starts executing
 *       when {@code start()} is called.</li>
 *   <li>Threads of one process share the heap; each thread has its own stack.</li>
 *   <li>Output order is NOT guaranteed: threads run concurrently.</li>
 * </ul>
 */
public class CreateThreadExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Create Thread Example ===");
        System.out.println();
        System.out.println("Main thread: " + Thread.currentThread().getName());
        System.out.println();

        // Option 1: Thread with an anonymous Runnable class.
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("[" + Thread.currentThread().getName() + "] anonymous Runnable");
            }
        }, "thread-1");

        // Option 2: Thread with a lambda Runnable (preferred, Java 8+).
        Thread t2 = new Thread(
                () -> System.out.println("[" + Thread.currentThread().getName() + "] lambda Runnable"),
                "thread-2");

        // Option 3: Thread subclass.
        Thread t3 = new MyWorkerThread("thread-3");

        // start() schedules the threads; join() makes main wait until each finishes.
        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println();
        System.out.println("Observation:");
        System.out.println("All three threads run CONCURRENTLY. The order of the three");
        System.out.println("printed lines may differ between runs - that is normal and is");
        System.out.println("the essence of concurrency.");
        System.out.println("A thread is only alive between start() and the end of run().");
    }

    /** A Thread subclass overrides run() with its own behavior. */
    private static class MyWorkerThread extends Thread {
        MyWorkerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.println("[" + getName() + "] Thread subclass");
        }
    }
}
