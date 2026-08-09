package com.example.javalab.basics;

/**
 * The most important beginner distinction in Java concurrency:
 *
 * <pre>
 *   thread.run();    // plain method call, executed in the CALLER thread
 *   thread.start();  // creates a NEW thread and runs run() inside it
 * </pre>
 *
 * <p>{@code run()} is just an ordinary method. Calling it directly gives you
 * zero concurrency - the work happens in whichever thread calls it.
 */
public class StartVsRunExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== start() vs run() Example ===");
        System.out.println();
        System.out.println("Main thread name: " + Thread.currentThread().getName());
        System.out.println();

        Runnable task = () -> System.out.println("  task executed in thread: "
                + Thread.currentThread().getName());

        Thread t = new Thread(task, "new-thread");

        // 1) run(): NO new thread is created.
        System.out.println("1) t.run()   -> runs in the CALLER thread:");
        t.run();

        // 2) start(): a brand new thread is created and scheduled.
        System.out.println("2) t.start() -> runs in a NEW thread:");
        t.start();

        t.join();   // wait for the new thread so the output is not cut off

        System.out.println();
        System.out.println("Observation:");
        System.out.println("- run()   executes the body in the calling thread: here 'main'.");
        System.out.println("- start() executes the body in a newly created thread: here 'new-thread'.");
        System.out.println("- A Thread can be started only ONCE. Calling start() twice");
        System.out.println("  throws IllegalThreadStateException.");
    }
}
