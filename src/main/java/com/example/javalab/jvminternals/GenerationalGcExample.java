package com.example.javalab.jvminternals;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes the generational hypothesis observable: a workload of short-lived
 * allocations drives many young-generation collections while the old
 * generation barely moves.
 *
 * <p>Recommended run (small heap makes the effect crisp), optionally with GC
 * logging to see the individual pauses:
 *
 * <pre>
 *   java -Xmx256m -Xlog:gc -cp target/classes com.example.javalab.jvminternals.GenerationalGcExample
 * </pre>
 *
 * <p>Look for {@code Pause Young (Normal)} lines in the log - young-only
 * cycles triggered by Eden filling up. The MXBean counters printed by the
 * example are the same counters you would watch in production monitoring.
 *
 * <p>Exact counts depend on heap size, collector and machine; watch the
 * RELATIONSHIP (young counts move, old counts stay near zero), not the
 * numbers.
 */
public class GenerationalGcExample {

    public static void main(String[] args) {
        System.out.println("=== Generational GC Example ===");
        System.out.println();

        GarbageCollectorMXBean young = findBean(true);
        GarbageCollectorMXBean old = findBean(false);
        System.out.println("Collector beans: young='" + (young == null ? "?" : young.getName())
                + "', old='" + (old == null ? "?" : old.getName()) + "'");
        System.out.println("Recommended run: java -Xmx256m -Xlog:gc -cp target/classes "
                + "com.example.javalab.jvminternals.GenerationalGcExample");
        System.out.println();

        long youngStart = count(young);
        long oldStart = count(old);

        // Long-lived payload: 64 MB that must survive -> reaches old age.
        List<byte[]> survivors = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            survivors.add(new byte[1024 * 1024]);
        }
        long youngAfterSurvivors = count(young);

        // Short-lived churn: continuous 1 MB garbage until another young GC runs.
        int churnedMB = 0;
        do {
            byte[] trash = new byte[1024 * 1024];
            churnedMB++;
        } while (count(young) <= youngAfterSurvivors && churnedMB < 4096);

        long youngEnd = count(young);
        long oldEnd = count(old);

        System.out.println("Workload: 64 MB long-lived survivors + " + churnedMB
                + " MB short-lived churn. Collection counts:");
        System.out.println("  young-gen : " + youngStart + " -> " + youngAfterSurvivors
                + " (survivors) -> " + youngEnd + "  | total delta " + (youngEnd - youngStart));
        System.out.println("  old-gen   : " + oldStart + " -> " + oldEnd
                + "  | total delta " + (oldEnd - oldStart));
        System.out.println();
        System.out.println("Anything that outlives enough young cycles is promoted to the");
        System.out.println("old generation: collected far less often, far more expensively.");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- The churn cost young-only collections (cheap: most of Eden");
        System.out.println("  is already dead when the cycle runs).");
        System.out.println("- Old-generation collections stayed flat: nothing escaped into");
        System.out.println("  old-gen in this short run - which is the weak generational");
        System.out.println("  hypothesis in one experiment: most objects die young.");
    }

    private static long count(GarbageCollectorMXBean bean) {
        return bean == null ? -1 : bean.getCollectionCount();
    }

    /**
     * Picks the young or old generation collector across common JVM names
     * (G1, Parallel, Serial, ZGC, Shenandoah).
     */
    private static GarbageCollectorMXBean findBean(boolean young) {
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = bean.getName();
            boolean isYoung = name.contains("Young") || name.contains("Scavenge")
                    || name.equals("Copy");
            boolean isOld = name.contains("Old") || name.contains("MarkSweep")
                    || name.contains("Complete");
            if (young ? isYoung : isOld) {
                return bean;
            }
        }
        return null;
    }
}