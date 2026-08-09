package com.example.javalab.virtualthread;

/**
 * The absolute basics of Virtual Threads (Java 21+).
 *
 * <p>A virtual thread is a JVM-managed lightweight thread. It is NOT an OS
 * thread: when it blocks, the JVM unmounts it from its carrier thread and
 * mounts another virtual thread instead. You can create hundreds of thousands
 * of them - they cost a few KB each, not ~1 MB of native stack.
 *
 * <p>API: {@code Thread.startVirtualThread(...)} or
 * {@code Thread.ofVirtual()...start(...)}.
 */
public class BasicVirtualThreadExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Basic Virtual Thread Example ===");
        System.out.println();

        // 1) One-liner: create and start a virtual thread.
        Thread vt1 = Thread.startVirtualThread(() ->
                System.out.println("  [virtual] " + Thread.currentThread().getName()
                        + " running, isVirtual=" + Thread.currentThread().isVirtual()));

        // 2) Builder: control the name.
        Thread vt2 = Thread.ofVirtual()
                .name("my-named-vt")
                .start(() -> System.out.println("  [virtual] " + Thread.currentThread().getName()
                        + " running, isVirtual=" + Thread.currentThread().isVirtual()));

        // 3) Compare with a platform thread.
        Thread platform = new Thread(() ->
                System.out.println("  [platform] " + Thread.currentThread().getName()
                        + " running, isVirtual=" + Thread.currentThread().isVirtual()),
                "plain-thread");

        vt1.join();
        vt2.join();
        platform.start();
        platform.join();

        System.out.println();
        System.out.println("Facts:");
        System.out.println("- Virtual threads are daemon by default: they do not keep the");
        System.out.println("  JVM alive on their own.");
        System.out.println("- They share the heap with everything else - thread safety");
        System.out.println("  rules are UNCHANGED.");
        System.out.println("- A blocking call (sleep, socket read, DB call) parks the");
        System.out.println("  virtual thread at almost no cost instead of occupying an");
        System.out.println("  OS thread.");
        System.out.println();
        System.out.println("Now run VirtualThreadExecutorExample to see 10,000 of them");
        System.out.println("running at the same time.");
    }
}
