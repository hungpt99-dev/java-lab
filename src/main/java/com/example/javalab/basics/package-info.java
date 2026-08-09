/**
 * Thread basics: creating threads, Runnable/Callable, start() vs run(),
 * join(), sleep(), and the six thread lifecycle states.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>Three ways to create a thread (Runnable, lambda, Thread subclass).</li>
 *   <li>{@code run()} vs {@code start()} - the classic beginner trap.</li>
 *   <li>Runnable (no result) vs Callable (result via Future).</li>
 *   <li>NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED states.</li>
 * </ul>
 *
 * <p>Intentionally shown mistakes: none - this package is purely introductory.
 *
 * <p>What to observe: output order is nondeterministic for concurrent threads;
 * {@code run()} always executes in the caller thread, never in a new one.
 *
 * <p>In real applications: prefer ExecutorService over raw threads; thread
 * states are what thread dumps show you when diagnosing production issues.
 */
package com.example.javalab.basics;
