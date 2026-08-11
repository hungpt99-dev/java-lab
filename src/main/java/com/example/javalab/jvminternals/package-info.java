/**
 * Runnable experiments that make JVM internals observable: bytecode,
 * class loading, stack frames, references vs objects, GC reachability,
 * generational collection, memory leaks, escape analysis, and the classic
 * boxed-type traps.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>Bytecode: {@code new}/{@code dup}/{@code ldc}/{@code invokespecial}
 *       sequences produced by {@code javac} (see BytecodeExample).</li>
 *   <li>Class loading: the bootstrap/platform/application hierarchy and lazy
 *       initialization (see ClassLoaderHierarchyExample).</li>
 *   <li>Interpretation vs JIT: throughput rising after warm-up
 *       (see WarmUpExample).</li>
 *   <li>Stack frames: recursion depth bounded by {@code -Xss}
 *       (see StackOverflowExample).</li>
 *   <li>References: a local variable holds a handle, not the object
 *       (see ObjectReferenceExample).</li>
 *   <li>Object lifecycle: zeroed defaults, field initializers, constructor
 *       order (see FieldInitializationExample).</li>
 *   <li>GC reachability: unreachable objects are reclaimed on the GC's
 *       schedule (see ReachabilityExample, CircularReferenceExample).</li>
 *   <li>Generational collection: young churn vs old promotion
 *       (see GenerationalGcExample).</li>
 *   <li>Memory leaks: accidental strong retention from a static root
 *       (see MemoryLeakExample).</li>
 *   <li>OOME flavors: heap vs direct buffers, and how to read which region
 *       failed (see OutOfMemoryAreasExample).</li>
 *   <li>Escape analysis: non-escaping allocations can be eliminated
 *       (see EscapeAnalysisExample).</li>
 *   <li>Boxed-type traps: Integer cache, null unboxing NPE, string pool,
 *       {@code finally} return (see IntegerCacheExample, UnboxingNpeExample,
 *       StringPoolExample, FinallyReturnExample).</li>
 * </ul>
 *
 * <p>Several experiments need JVM flags or {@code javap} to show their full
 * effect; the README section "JVM Internals experiments" lists the exact
 * commands. Every example is safe to run: allocation churn is bounded,
 * OutOfMemoryErrors are caught, and recursion stops at the error.
 */
package com.example.javalab.jvminternals;
