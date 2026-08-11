package com.example.javalab.jvminternals;

import java.lang.ref.WeakReference;

/**
 * The classic folklore bug: two objects referencing each other should
 * "leak" because neither reference count could ever reach zero.
 *
 * <p>Java's collector is a TRACING collector: it walks from GC Roots and
 * reclaims everything not visited - so an unreachable cycle (A -> B -> A
 * with no path from any root) is reclaimed like any other garbage. This
 * experiment proves it by watching both objects of a cycle get collected.
 *
 * <p>Reference-counted runtimes (early Python, Objective-C) need extra
 * cycle-detection machinery here; a tracing collector does not.
 */
public class CircularReferenceExample {

    static final class A {
        B b;
    }

    static final class B {
        A a;
    }

    public static void main(String[] args) {
        System.out.println("=== Circular Reference Example ===");
        System.out.println();

        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;                       // A <-> B reference each other

        WeakReference<A> refA = new WeakReference<>(a);
        WeakReference<B> refB = new WeakReference<>(b);

        System.out.println("Cycle created: A.b -> B, B.a -> A.");
        System.out.println("  refA alive: " + (refA.get() != null));
        System.out.println("  refB alive: " + (refB.get() != null));
        System.out.println();

        a = null;
        b = null;                      // no root points at either object anymore
        System.out.println("a = null; b = null;   (no GC Root reaches the cycle)");
        System.out.println("  immediately after: refA alive: " + (refA.get() != null)
                + ", refB alive: " + (refB.get() != null));
        System.out.println("  -> still alive, but only as unreachable garbage.");
        System.out.println();

        // Allocation pressure + explicit GC requests until both are cleared.
        int churnedMB = 0;
        while ((refA.get() != null || refB.get() != null) && churnedMB < 4096) {
            byte[] trash = new byte[1024 * 1024];
            churnedMB++;
            if (churnedMB % 32 == 0) {
                System.gc();
            }
        }

        System.out.println("After " + churnedMB + " MB of churn:");
        System.out.println("  refA alive: " + (refA.get() != null));
        System.out.println("  refB alive: " + (refB.get() != null));
        System.out.println("  -> BOTH collected. The mutual references did not save them.");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- 'A references B' and 'B references A' are edges between");
        System.out.println("  garbage: the mark phase never reaches them.");
        System.out.println("- Tracing GC answers 'reachable from a root?', never");
        System.out.println("  'who references me?' - which is exactly why cycles die.");
        System.out.println("- A reference-counted collector would permanently leak this");
        System.out.println("  pair without additional cycle detection.");
    }
}