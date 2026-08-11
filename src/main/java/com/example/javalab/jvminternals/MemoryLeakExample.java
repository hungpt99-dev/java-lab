package com.example.javalab.jvminternals;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Shows what a Java memory leak actually is: not a bug in the GC, but the
 * program accidentally KEEPING objects reachable. A static map is a GC Root,
 * so everything inside it stays reachable forever - while the same data in a
 * {@link WeakHashMap} is reclaimed as soon as it stops being used.
 *
 * <p>What to observe: after churn, the weak map has shed most entries (the
 * GC reclaimed them), but the strong map still holds all 2000 - because a
 * static field is a root. The leak is the strong map's missing eviction
 * policy, not a failing collector.
 */
public class MemoryLeakExample {

    /** Static field = a GC Root. Everything reachable from here never dies. */
    private static final Map<String, byte[]> STRONG_CACHE = new HashMap<>();

    private static final int ENTRIES = 2000;

    public static void main(String[] args) {
        System.out.println("=== Memory Leak Example ===");
        System.out.println();

        Map<String, byte[]> weakCache = new WeakHashMap<>();

        for (int i = 0; i < ENTRIES; i++) {
            // Distinct key/value instances per map: the weak map's keys must
            // not be reachable through the strong map, or they would never die.
            STRONG_CACHE.put(new String("user-" + i), new byte[1024]);   // 1 KB each
            weakCache.put(new String("user-" + i), new byte[1024]);
        }
        System.out.println("Inserted " + ENTRIES + " entries (1 KB each) into:");
        System.out.println("  STRONG_CACHE (static field = GC Root)");
        System.out.println("  weakCache    (WeakHashMap, keys weakly reachable)");
        System.out.println();
        System.out.println("  before churn: strong=" + STRONG_CACHE.size()
                + ", weak=" + weakCache.size());
        System.out.println();

        // Churn until the weak map loses entries or a safety budget is used up.
        int rounds = 0;
        while (weakCache.size() >= ENTRIES && rounds < 50) {
            byte[] trash = new byte[1024 * 1024];
            System.gc();
            rounds++;
        }

        System.out.println("After " + rounds + " churn rounds (1 MB garbage + System.gc() each):");
        System.out.println("  strong=" + STRONG_CACHE.size() + "   (still every entry: REACHABLE)");
        System.out.println("  weak  =" + weakCache.size() + "   (entries cleared: unreachable)");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- The GC is working perfectly here. The strong map is the leak:");
        System.out.println("  a static root keeps every entry reachable by construction.");
        System.out.println("- The fix is an eviction policy / ownership discipline, not a");
        System.out.println("  bigger -Xmx.  WeakReference-based structures are the 'make it");
        System.out.println("  cancellable' alternative shown by weakCache.");
    }
}