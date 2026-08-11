package com.example.javalab.jvminternals;

import java.lang.ref.WeakReference;

/**
 * Shows that {@code user = null} does NOT delete anything: the object stays
 * fully intact until the garbage collector runs and finds it unreachable.
 *
 * <p>A {@link WeakReference} lets us watch the collector from the outside:
 * while the object is strongly reachable the reference is cleared only when
 * the GC proves the object unreachable and reclaims it.
 *
 * <p>What to observe:
 * <ul>
 *   <li>Immediately after {@code big = null}, the object is still there -
 *       the assignment freed nothing.</li>
 *   <li>After allocation pressure (and a {@code System.gc()} request, which
 *       is exactly that: a request, not a command), the weak reference is
 *       cleared: the collector reclaimed the unreachable object.</li>
 * </ul>
 */
public class ReachabilityExample {

    public static void main(String[] args) {
        System.out.println("=== Reachability Example ===");
        System.out.println();

        byte[] big = new byte[16 * 1024 * 1024];   // 16 MB object
        WeakReference<byte[]> ref = new WeakReference<>(big);

        System.out.println("Allocated a 16 MB byte[]; strong ref 'big' + weak ref.");
        System.out.println("  ref.get() != null : " + (ref.get() != null) + "   (reachable)");
        System.out.println();

        big = null;                                // the 'user = null' moment
        System.out.println("big = null;   (the analog of user = null)");
        System.out.println("  right after: ref.get() != null : " + (ref.get() != null));
        System.out.println("  -> the assignment deleted NOTHING. The object is still");
        System.out.println("     intact in memory, just no longer strongly reachable.");
        System.out.println();

        // Apply allocation pressure so the collector actually runs.
        int churnedMB = 0;
        while (ref.get() != null && churnedMB < 4096) {
            byte[] trash = new byte[1024 * 1024];  // 1 MB of garbage
            churnedMB++;
            if (churnedMB % 32 == 0) {
                System.gc();                       // a REQUEST, not a command
            }
        }

        System.out.println("After " + churnedMB + " MB of allocation churn:");
        System.out.println("  ref.get() != null : " + (ref.get() != null));
        System.out.println("  -> the collector reclaimed the unreachable object,");
        System.out.println("     on its own schedule (here: under churn pressure).");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- 'x = null' removes ONE path to the object. The GC decides");
        System.out.println("  WHEN it runs; the exact moment is not in any spec.");
        System.out.println("- Reachability, not nulling, is what the GC checks: reachable");
        System.out.println("  objects keep their memory, unreachable ones may be reclaimed");
        System.out.println("  in the next cycle - or a later one.");
    }
}
