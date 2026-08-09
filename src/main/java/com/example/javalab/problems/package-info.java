/**
 * Reproducible, controlled versions of the classic production concurrency
 * failures.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>Deadlock: circular lock acquisition, JVM detection, thread dumps.</li>
 *   <li>Thread/task starvation: long tasks starving short tasks in a shared pool.</li>
 *   <li>ThreadLocal leaks: stale data + memory leaks with reused pool threads.</li>
 *   <li>Lost exceptions: submit() hides failures inside the Future.</li>
 *   <li>Blocking shared pools: one slow downstream takes down fast work.</li>
 * </ul>
 *
 * <p>Intentionally shown mistakes: every example here is a bug reproduction;
 * each class ends with the production-grade fix or prevention rules.
 *
 * <p>Safety notes: DeadlockExample uses daemon threads so the JVM always
 * exits; StarvationExample and BlockingSharedPoolExample are bounded by
 * timers and always terminate.
 */
package com.example.javalab.problems;
