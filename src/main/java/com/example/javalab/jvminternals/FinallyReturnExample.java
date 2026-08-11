package com.example.javalab.jvminternals;

/**
 * The {@code finally}-return trap: the JVM compiles a {@code finally} block
 * into EVERY exit path of the {@code try}, and a {@code return} that
 * executes wins - silently discarding the pending value, or even swallowing
 * an in-flight exception.
 *
 * <p>After compiling, disassemble to see the duplicated exit paths:
 *
 * <pre>
 *   javap -c -p target/classes/com/example/javalab/jvminternals/FinallyReturnExample.class
 * </pre>
 *
 * <p>You will see the {@code finally} body duplicated after the normal
 * {@code return} and after the exception handler paths.
 */
public class FinallyReturnExample {

    static int probe() {
        try {
            return 1;
        } finally {
            return 2;                      // executes after 'return 1' was evaluated
        }
    }

    static int swallowsException() {
        try {
            throw new IllegalStateException("boom from try");
        } finally {
            return 0;                      // the exception is discarded with this return
        }
    }

    public static void main(String[] args) {
        System.out.println("=== finally-Return Example ===");
        System.out.println();

        System.out.println("probe()              : " + probe()
                + "   (you might expect 1)");
        System.out.println("swallowsException()  : " + swallowsException()
                + "   (the IllegalStateException disappeared)");
        System.out.println();

        System.out.println("Why:");
        System.out.println("- The JVM duplicates the finally body into every exit path:");
        System.out.println("  the normal one and each exception-handler path.");
        System.out.println("- 'return 2' in finally executes AFTER 'return 1' has been");
        System.out.println("  evaluated but BEFORE the method returns - and wins.");
        System.out.println("- Same mechanics swallow exceptions: a return in finally is");
        System.out.println("  as suspicious as an empty 'catch (Exception e) {}'.");
        System.out.println();
        System.out.println("Inspect it yourself:");
        System.out.println("  javap -c -p target/classes/com/example/javalab/jvminternals/FinallyReturnExample.class");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("- Cleanup belongs in finally; RETURNING from finally is a bug");
        System.out.println("  pattern - prefer a try/finally that preserves return and");
        System.out.println("  exception flow in both directions.");
    }
}