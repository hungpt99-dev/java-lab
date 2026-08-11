package com.example.javalab.jvminternals;

/**
 * Shows the object initialization order (JLS 12.5): zeroed defaults ->
 * implicit {@code super()} invocation -> explicit field initializers ->
 * constructor body.
 *
 * <p>Fields that no constructor ever assigns still hold their default values
 * ({@code 0}, {@code false}, {@code null}) because the freshly allocated
 * memory is zeroed before any constructor runs - that is a language
 * guarantee, not a HotSpot accident.
 *
 * <p>Note the subtlety the article highlights: the implicit {@code super()}
 * call is the FIRST step of every constructor, so the superclass
 * constructor runs before THIS class's field initializers (observable here
 * as {@code [1] super() -> [2] field initializer -> [3] constructor body}).
 */
public class FieldInitializationExample {

    static class Parent {
        Parent() {
            System.out.println("  [1] super() constructor body runs (implicit super() first)");
        }
    }

    static final class Widget extends Parent {
        int count;                       // never assigned anywhere
        boolean enabled;                 // never assigned anywhere
        String label;                    // never assigned anywhere

        // Explicit field initializer - runs after super() returns, before the body:
        String color = initColor("  [2] explicit field initializer: color = \"default\"");

        String side;

        Widget(String side) {
            // javac inserts an implicit super() call BEFORE this body:
            System.out.println("  [3] Widget constructor body runs");
            this.side = side;
        }

        static String initColor(String message) {
            System.out.println(message);
            return "default";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Field Initialization Example ===");
        System.out.println();
        System.out.println("Creating a Widget, watch the initialization order:");
        System.out.println();

        Widget w = new Widget("left");

        System.out.println();
        System.out.println("Values right after construction:");
        System.out.println("  count   = " + w.count + "   (zeroed default)");
        System.out.println("  enabled = " + w.enabled + "   (zeroed default)");
        System.out.println("  label   = " + w.label + "   (zeroed default)");
        System.out.println("  color   = " + w.color + "   (explicit initializer)");
        System.out.println("  side    = " + w.side + "   (constructor)");
        System.out.println();

        System.out.println("Observation:");
        System.out.println("- The order is: zeroed memory -> super() -> field");
        System.out.println("  initializers -> constructor body.");
        System.out.println("- super() is the implicit FIRST step of every constructor,");
        System.out.println("  so it runs before this class's field initializers.");
        System.out.println("- Unassigned fields read 0 / false / null: the allocation");
        System.out.println("  was zeroed before any code touched it (language guarantee).");
    }
}