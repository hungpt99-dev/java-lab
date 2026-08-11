package com.example.javalab.jvminternals;

import java.lang.management.ManagementFactory;

/**
 * Demonstrates escape analysis: when an object never escapes its method,
 * HotSpot's C2 compiler is allowed to eliminate the allocation entirely
 * (scalar replacement) - no heap object, no TLAB bump, no GC interaction.
 *
 * <p>Run the comparison - same code, flag toggled, GC log on:
 *
 * <pre>
 *   # Escape analysis ON (default): expect very few or zero GC pauses
 *   java -Xmx64m -Xlog:gc -cp target/classes com.example.javalab.jvminternals.EscapeAnalysisExample
 *
 *   # Escape analysis OFF: the same loop allocates real objects past the
 *   # 64 MB heap, so young GC pauses appear and the run slows down
 *   java -Xmx64m -XX:-DoEscapeAnalysis -Xlog:gc -cp target/classes com.example.javalab.jvminternals.EscapeAnalysisExample
 * </pre>
 *
 * <p>In the second run look for {@code Pause Young (Normal)} lines in the GC
 * log. Note: the allocation count is JIT-state dependent, so keep the log
 * output visible and watch the TREND (pauses vs no pauses).
 */
public class EscapeAnalysisExample {

    /** A tiny 2-field object. Never escapes the loop below. */
    static final class Point {
        final int x;
        final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static long accumulate(long iterations) {
        long sum = 0;
        for (long i = 0; i < iterations; i++) {
            // p is created locally and never escapes: no field store, no
            // method call, no return, no other thread.
            Point p = new Point((int) i, (int) i + 1);
            sum += p.x + p.y;
        }
        return sum;
    }

    public static void main(String[] args) {
        long iterations = args.length > 0 ? Long.parseLong(args[0]) : 100_000_000L;

        System.out.println("=== Escape Analysis Example ===");
        System.out.println();
        System.out.println("VM input args: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
        System.out.println("iterations   : " + iterations);
        System.out.println();
        System.out.println("Loop: Point p = new Point(i, i+1);  (never escapes)");
        System.out.println("Compile the method first (warm-up), then measure:");
        System.out.println();

        accumulate(2_000_000L);            // warm-up: let C2 compile the loop first
        long start = System.nanoTime();
        long sum = accumulate(iterations);
        long elapsed = System.nanoTime() - start;

        System.out.printf("timed run: finished in %.3f s%n", elapsed / 1e9);
        System.out.printf("             %.2f ns per iteration, sum=%d%n",
                (double) elapsed / iterations, sum);
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- This form of the loop performs no observable heap");
        System.out.println("  allocation while escape analysis is ON.");
        System.out.println("- Repeat with -XX:-DoEscapeAnalysis (and -Xmx64m) and the");
        System.out.println("  same loop starts allocating real 24-byte Points:");
        System.out.println("  young GC pauses appear in the GC log and the wall time");
        System.out.println("  grows - the JIT is allowed to eliminate allocations,");
        System.out.println("  but it is not OBLIGED to.");
    }
}