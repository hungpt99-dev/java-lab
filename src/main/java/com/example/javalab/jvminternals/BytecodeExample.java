package com.example.javalab.jvminternals;

/**
 * The exact {@code new User("Hung")} line from the "How Java works" article,
 * plus the observable runtime behavior of the instructions it compiles to.
 *
 * <p>After compiling, disassemble this class and read the article's bytecode
 * walkthrough against the real output:
 *
 * <pre>
 *   javap -c -p target/classes/com/example/javalab/jvminternals/BytecodeExample.class
 * </pre>
 *
 * <p>Look for the sequence {@code new / dup / ldc / invokespecial / astore_1}
 * in {@code main}, and {@code iload / iadd} in {@code sum}. These are the
 * instructions described in the article: allocate, duplicate the reference,
 * load the string constant, call the constructor, store the reference.
 */
public class BytecodeExample {

    /** A tiny stand-in for the article's {@code User} class. */
    static final class User {
        private final String name;

        User(String name) {
            this.name = name;
        }

        String name() {
            return name;
        }
    }

    /** Compiles to iload_1, iload_2, iadd - arithmetic on the operand stack. */
    static int sum(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println("=== Bytecode Example ===");
        System.out.println();
        System.out.println("Java source:");
        System.out.println("  User user = new User(\"Hung\");");
        System.out.println("  process(user);");
        System.out.println();
        System.out.println("Running the same code:");
        User user = new User("Hung");      // new / dup / ldc / invokespecial / astore_1
        System.out.println("  user.name = " + user.name());
        System.out.println("  sum(2, 3) = " + sum(2, 3));
        System.out.println();
        System.out.println("javac produced no machine code - it produced one .class");
        System.out.println("file of bytecode instructions for the fictional JVM machine.");
        System.out.println();
        System.out.println("Inspect it yourself:");
        System.out.println("  javap -c -p target/classes/com/example/javalab/jvminternals/BytecodeExample.class");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("- 'new' allocates; 'dup' keeps a copy of the reference for the");
        System.out.println("  constructor call; 'invokespecial' runs <init>; 'astore_1'");
        System.out.println("  saves the surviving reference into local slot 1.");
        System.out.println("- Every one of those instructions is still just a description.");
        System.out.println("  The JVM decides later how to execute them on this CPU.");
    }
}
