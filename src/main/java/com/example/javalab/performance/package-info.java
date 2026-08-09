/**
 * Performance: what thread count actually buys you.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>CPU-bound work: bounded by CORES - more threads add overhead.</li>
 *   <li>I/O-bound work: higher concurrency overlaps waiting - throughput scales.</li>
 *   <li>Excessive threads: switching + cache thrashing can make things WORSE.</li>
 * </ul>
 *
 * <p>Intentionally shown mistake: the "more threads = faster" assumption.
 *
 * <p>All timings are demonstration-grade (machine-specific, JIT-dependent).
 * The goal is the TREND, not universal benchmark numbers.
 */
package com.example.javalab.performance;
