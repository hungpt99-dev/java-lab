/**
 * Synchronization and shared-state correctness.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>Race conditions on a shared counter ({@code count++}).</li>
 *   <li>Atomicity, visibility and ordering (the three JMM pillars).</li>
 *   <li>{@code synchronized} (mutual exclusion + visibility).</li>
 *   <li>{@code volatile}: visibility ONLY - it does NOT make {@code count++} atomic.</li>
 *   <li>{@link java.util.concurrent.atomic.AtomicInteger}: lock-free CAS counters.</li>
 *   <li>{@link java.util.concurrent.locks.ReentrantLock}: tryLock, fairness, conditions.</li>
 * </ul>
 *
 * <p>Intentionally shown mistakes:
 * <ul>
 *   <li>{@link RaceConditionExample} - broken counter without synchronization.</li>
 *   <li>{@link VolatileExample} - "volatile makes counters safe" misconception.</li>
 * </ul>
 *
 * <p>What to observe: RaceConditionExample loses increments (nondeterministic);
 * every synchronized/atomic/lock version is always correct.
 *
 * <p>In real applications: use {@code volatile} for flags, {@code Atomic*}
 * for counters, {@code synchronized} for short critical sections, and
 * {@code ReentrantLock} when you need timeouts or fairness.
 */
package com.example.javalab.synchronization;
