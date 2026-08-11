package com.example.javalab.jvminternals;

/**
 * The two lives of "hello": literals and compile-time constants are
 * interned into the shared string pool, while {@code new String(...)} and
 * runtime concatenation produce fresh objects. {@code ==} compares identity,
 * so it reveals which life a string is living.
 */
public class StringPoolExample {

    public static void main(String[] args) {
        System.out.println("=== String Pool Example ===");
        System.out.println();

        String a = "hello";                  // literal -> interned
        String b = "hello";                  // same pooled instance
        String c = new String("hello");      // fresh object, pool untouched

        System.out.println("a == b            : " + (a == b)
                + "   (2 literals, 1 pooled instance)");
        System.out.println("a == c            : " + (a == c)
                + "   (new String() is a separate object)");
        System.out.println("a.equals(c)       : " + a.equals(c)
                + "   (content is what you usually mean)");
        System.out.println();

        String folded = "hel" + "lo";        // compile-time constant folding
        String prefix = "hel";
        String built = prefix + "lo";        // runtime concatenation

        System.out.println("a == (\"hel\"+\"lo\") : " + (a == folded)
                + "   (javac folded the constants -> pooled)");
        System.out.println("a == (prefix+\"lo\") : " + (a == built)
                + "   (runtime concat -> fresh object)");
        System.out.println("a == c.intern()   : " + (a == c.intern())
                + "   (intern() returns the canonical pooled instance)");
        System.out.println();

        System.out.println("identityHashCode: a=" + System.identityHashCode(a)
                + " b=" + System.identityHashCode(b)
                + " c=" + System.identityHashCode(c)
                + " built=" + System.identityHashCode(built));
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- Literals + compile-time constants live in the shared");
        System.out.println("  string pool; 'new' and runtime concatenation do not.");
        System.out.println("- '==' on strings is identity, so only interned/constant");
        System.out.println("  strings can compare equal - never rely on it for content.");
        System.out.println("- The pool lives IN the heap since Java 7: thousands of");
        System.out.println("  unique intern()ed strings consume real heap.");
    }
}