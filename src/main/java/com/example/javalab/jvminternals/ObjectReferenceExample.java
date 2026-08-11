package com.example.javalab.jvminternals;

/**
 * Proves the core distinction of the article: a local variable holds a
 * REFERENCE (a handle), not the object. The object lives in GC-managed
 * memory; several variables can point to the same object.
 *
 * <p>What to observe:
 * <ul>
 *   <li>{@code u2 = u1} copies the reference, so both variables show the
 *       same {@code System.identityHashCode} - they are the same object.</li>
 *   <li>Mutating through {@code u2} is visible through {@code u1}.</li>
 *   <li>{@code u2 = null} drops one path to the object; the object itself is
 *       untouched and still reachable through {@code u1}.</li>
 * </ul>
 */
public class ObjectReferenceExample {

    static final class User {
        String name;

        User(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Object Reference Example ===");
        System.out.println();

        User u1 = new User("Hung");
        User u2 = u1;                     // copies the reference, NOT the object

        System.out.println("identityHashCode(u1) = " + System.identityHashCode(u1));
        System.out.println("identityHashCode(u2) = " + System.identityHashCode(u2));
        System.out.println("u1 == u2            = " + (u1 == u2) + "   (same identity)");
        System.out.println();

        u2.name = "changed";              // mutate THROUGH u2
        System.out.println("u2.name = \"changed\";");
        System.out.println("u1.name now reads   = " + u1.name
                + "   (same object, mutation visible)");
        System.out.println();

        u2 = null;                        // drops one path only
        System.out.println("u2 = null;");
        System.out.println("u1.name still reads = " + u1.name
                + "   (object untouched, still alive)");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- 'u2 = u1' copies a handle; both handles point to ONE");
        System.out.println("  object on the heap. Nulling one handle does nothing to");
        System.out.println("  the object - only the GC reclaims it, later, and only");
        System.out.println("  when no reachable handle exists anymore.");
    }
}
