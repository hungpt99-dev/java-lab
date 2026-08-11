package com.example.javalab.jvminternals;

import java.lang.management.ManagementFactory;

/**
 * Demonstrates JIT warm-up: the same method, measured in epochs, gets faster
 * after the JVM compiles it to native code.
 *
 * <p>Run with compilation logging to watch {@code work} being compiled while
 * the epochs run:
 *
 * <pre>
 *   java -Xlog:jit+compilation -cp target/classes com.example.javalab.jvminternals.WarmUpExample
 * </pre>
 *
 * <p>Note: the {@code jit+compilation} log tag is the JDK 24+ name; on
 * JDK 21-23 use {@code -Xlog:compilation} instead.
 *
 * <p>The method starts interpreted, gets profiled, is compiled by C1, and -
 * if it stays hot - by C2. Expect the first epoch(s) to be the slowest and
 * later epochs to plateau at a lower ns/call. Exact numbers and the epoch
 * where the speedup appears are machine- and JIT-state-dependent: watch the
 * TREND, not the values.
 */
public class WarmUpExample {

    /** Pure arithmetic so C2 can compile it, but nothing can be folded away. */
    static long work(long v) {
        return (v * 31L + 7L) ^ (v >>> 13L);
    }

    public static void main(String[] args) {
        int epochs = 6;
        long callsPerEpoch = 5_000_000L;
        if (args.length >= 1) {
            epochs = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            callsPerEpoch = Long.parseLong(args[1]);
        }

        System.out.println("=== Warm-Up / JIT Example ===");
        System.out.println();
        System.out.println("VM input args: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
        System.out.println("epochs        : " + epochs);
        System.out.println("calls/epoch   : " + callsPerEpoch);
        System.out.println();

        long acc = 0;
        for (int e = 1; e <= epochs; e++) {
            long start = System.nanoTime();
            for (long i = 0; i < callsPerEpoch; i++) {
                acc += work(i);
            }
            long elapsed = System.nanoTime() - start;
            double nsPerCall = (double) elapsed / callsPerEpoch;
            System.out.printf("epoch %d: %10.2f ns/call%n", e, nsPerCall);
        }
        System.out.println("checksum (keeps the loop alive): " + acc);
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- Later epochs are usually faster: the method moved from");
        System.out.println("  interpreted bytecode to JIT-compiled native code.");
        System.out.println("- Run with -Xlog:jit+compilation (JDK 24+; use");
        System.out.println("  -Xlog:compilation on JDK 21-23) to see 'compile' events for");
        System.out.println("  WarmUpExample.work() appear mid-run.");
        System.out.println("- This is why cold calls and micro-benchmarks without a");
        System.out.println("  warm-up phase do not measure steady-state performance.");
    }
}
