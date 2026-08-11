package com.example.javalab.jvminternals;

import java.lang.management.ManagementFactory;

/**
 * Shows that each thread has a fixed-size JVM stack: unbounded recursion
 * exhausts it and the JVM throws {@link StackOverflowError}.
 *
 * <p>Compare how many frames fit when the stack is small vs large:
 *
 * <pre>
 *   java -Xss256k -cp target/classes com.example.javalab.jvminternals.StackOverflowExample
 *   java -Xss4m   -cp target/classes com.example.javalab.jvminternals.StackOverflowExample
 * </pre>
 *
 * <p>The frame count printed by the example is proportional to the stack
 * size: a few thousand frames with 256 KB, far more with 4 MB. The error is
 * about running out of STACK, not heap - the heap is untouched here.
 */
public class StackOverflowExample {

    /** Incremented by every nested call; reset per run by the JVM defaults. */
    private static long frames;

    static void recurse() {
        frames++;
        recurse();
    }

    public static void main(String[] args) {
        System.out.println("=== Stack Overflow Example ===");
        System.out.println();
        System.out.println("VM input args: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
        System.out.println("Thread stack size is fixed per thread (HotSpot default");
        System.out.println("~512 KB - 1 MB; tune with -Xss). Every nested call pushes");
        System.out.println("one stack frame onto the current thread's stack.");
        System.out.println();

        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError thrown after " + frames + " nested calls.");
            System.out.println("Frame depth: " + frames);
        }

        System.out.println();
        System.out.println("Compare:");
        System.out.println("  java -Xss256k -cp target/classes com.example.javalab.jvminternals.StackOverflowExample");
        System.out.println("  java -Xss4m   -cp target/classes com.example.javalab.jvminternals.StackOverflowExample");
        System.out.println("-> smaller stack, fewer frames; bigger stack, more frames.");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("- The JVM did not run out of heap; it ran out of STACK.");
        System.out.println("- Frames are not reused: deep recursion grows the stack");
        System.out.println("  until the thread's fixed-size stack is exhausted.");
        System.out.println("- On the main thread, -Xss applies at launch; worker threads");
        System.out.println("  can be given their own stack size via the Thread constructor.");
    }
}
