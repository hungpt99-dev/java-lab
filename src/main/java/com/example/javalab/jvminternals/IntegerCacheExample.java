package com.example.javalab.jvminternals;

/**
 * The Integer cache surprise: autoboxing calls
 * {@code Integer.valueOf()}, which returns SHARED cached instances for the
 * default range -128..127. So {@code ==} on boxed values compares identity
 * and flips from true to false depending on the VALUE.
 *
 * <p>Try the tunable knob:
 *
 * <pre>
 *   java -XX:AutoBoxCacheMax=2000 -cp target/classes com.example.javalab.jvminternals.IntegerCacheExample
 * </pre>
 *
 * <p>With the cache extended, {@code 200 == 200} becomes {@code true} too.
 */
public class IntegerCacheExample {

    public static void main(String[] args) {
        System.out.println("=== Integer Cache Example ===");
        System.out.println();

        Integer a = 100;                  // autoboxing == Integer.valueOf(100)
        Integer b = 100;                  // cached: same instance as a
        Integer c = 200;                  // outside the default cache range
        Integer d = 200;                  // a NEW instance

        System.out.println("a == b : " + (a == b) + "     (2 x Integer.valueOf(100), cached)");
        System.out.println("c == d : " + (c == d) + "     (2 x Integer.valueOf(200), NOT cached)");
        System.out.println();
        System.out.println("identityHashCode: a=" + System.identityHashCode(a)
                + " b=" + System.identityHashCode(b)
                + " c=" + System.identityHashCode(c)
                + " d=" + System.identityHashCode(d));
        System.out.println();

        Integer edge1 = Integer.valueOf(-128);
        Integer edge2 = Integer.valueOf(-128);
        Integer edge3 = Integer.valueOf(-129);
        Integer edge4 = Integer.valueOf(-129);
        System.out.println("valueOf(-128) == valueOf(-128) : " + (edge1 == edge2)
                + "   (cache boundary, inside)");
        System.out.println("valueOf(-129) == valueOf(-129) : " + (edge3 == edge4)
                + "   (outside the cache)");
        System.out.println();

        Boolean bt = Boolean.valueOf(true);
        System.out.println("Boolean.valueOf(true) == Boolean.TRUE : " + (bt == Boolean.TRUE)
                + "   (two values, always cached)");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- '==' on boxed types compares IDENTITY, never value.");
        System.out.println("- Integer.valueOf caches -128..127 by default, so equality");
        System.out.println("  results depend on the value - counterintuitive by design.");
        System.out.println("- Always compare numbers with equals() or unbox first;");
        System.out.println("  -XX:AutoBoxCacheMax extends (never shrinks below 127) the range.");
    }
}