package com.example.javalab.jvminternals;

import com.sun.net.httpserver.HttpServer;
import java.lang.management.ManagementFactory;

/**
 * Shows the three-tier class loader hierarchy and proves that classes are
 * loaded lazily.
 *
 * <p>Run with {@code -Xlog:class+load} to watch the JVM load classes one by
 * one, on first use:
 *
 * <pre>
 *   java -Xlog:class+load -cp target/classes com.example.javalab.jvminternals.ClassLoaderHierarchyExample
 * </pre>
 *
 * <p>The interesting lines: {@code ClassLoaderHierarchyExample} is loaded
 * right at the start (it is the entry point), while {@code Lazy} is loaded
 * only at the moment {@code Lazy.tag()} is first called - not before.
 */
public class ClassLoaderHierarchyExample {

    /** Loaded and initialized only when first actively used. */
    private static final class Lazy {
        static {
            System.out.println("  [static init] Lazy initialized (first active use)");
        }

        static String tag() {
            return "lazy-loaded-class";
        }
    }

    /** Loaded at startup: it is the entry point. */
    static {
        System.out.println("  [static init] ClassLoaderHierarchyExample initialized (entry point)");
    }

    public static void main(String[] args) {
        System.out.println("=== Class Loader Hierarchy Example ===");
        System.out.println();
        System.out.println("VM input args: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
        System.out.println();

        System.out.println("Who loads what (null = bootstrap loader):");
        printLoader("our own class             ", ClassLoaderHierarchyExample.class);
        printLoader("java.lang.String          ", String.class);
        printLoader("com.sun.net.httpserver.HttpServer", HttpServer.class);
        printLoader("our Lazy inner class      ", Lazy.class);
        System.out.println();

        System.out.println("Lazy loading: at this point 'Lazy' has not been touched.");
        System.out.println("Calling Lazy.tag(): " + Lazy.tag());
        System.out.println();
        System.out.println("Run again with:");
        System.out.println("  java -Xlog:class+load -cp target/classes com.example.javalab.jvminternals.ClassLoaderHierarchyExample");
        System.out.println("and watch WHEN each class is loaded - the entry point immediately,");
        System.out.println("everything else on first use. Classes are also verified and");
        System.out.println("linked before any of their code runs.");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("- String (java.base) -> bootstrap loader (prints null).");
        System.out.println("- HttpServer (jdk.httpserver, a platform module) -> platform loader.");
        System.out.println("- Our classes -> application (system) loader.");
        System.out.println("- Static initialization happens lazily, at first active use.");
        System.out.println("- Loading never travels 'down' the hierarchy: an application");
        System.out.println("  loader delegates to its parent first (parent-first model).");
    }

    private static void printLoader(String what, Class<?> c) {
        ClassLoader loader = c.getClassLoader();
        String name = loader == null ? "null (bootstrap)" : loader.getName();
        System.out.printf("  %-44s -> %s%n", what, name);
    }
}
