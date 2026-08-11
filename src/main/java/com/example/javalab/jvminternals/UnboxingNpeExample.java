package com.example.javalab.jvminternals;

/**
 * The null-unboxing trap: {@code int result = value;} compiles to
 * {@code value.intValue()}, and calling a method on {@code null} throws
 * a {@link NullPointerException}. The line "always throws", no matter how
 * obvious 0 it looks.
 *
 * <p>On JDK 14+ the JVM names the culprit, which makes this failure easy to
 * diagnose when it arrives through an autoboxing boundary (a raw query
 * result, a JSON parser default, a Hazelcast style miss, ...).
 */
public class UnboxingNpeExample {

    public static void main(String[] args) {
        System.out.println("=== Null Unboxing NPE Example ===");
        System.out.println();

        Integer value = null;

        try {
            int result = value;            // compiles to: value.intValue()
            System.out.println("Unreachable: result = " + result);
        } catch (NullPointerException e) {
            System.out.println("int result = value;  (value == null)");
            System.out.println("-> NullPointerException thrown.");
            System.out.println("   message: " + e.getMessage());
        }

        System.out.println();
        System.out.println("The hidden call is the whole story:");
        System.out.println("  int result = value;   ==   int result = value.intValue();");
        System.out.println("  null.intValue()  ->  NullPointerException");
        System.out.println();

        // The defensive pattern: validate before unboxing.
        Integer maybe = null;
        if (maybe != null) {
            System.out.println("safe path: got " + maybe);
        } else {
            System.out.println("safe path: null detected, no unboxing attempted");
        }
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- Autoboxing boundaries hide a method call that can fail:");
        System.out.println("  every 'int' received from parsed/legacy data went through");
        System.out.println("  one. Validate nullability at the boundary, or use");
        System.out.println("  Optional/primitive streams where the contract allows it.");
    }
}