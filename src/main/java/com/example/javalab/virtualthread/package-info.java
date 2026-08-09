/**
 * Virtual Threads (Java 21+).
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>{@code Thread.startVirtualThread} and {@code Thread.ofVirtual()}.</li>
 *   <li>{@code Executors.newVirtualThreadPerTaskExecutor()} - one VT per task.</li>
 *   <li>Platform vs virtual threads on blocking workloads.</li>
 *   <li>Blocking I/O: queueing disappears, latency drops to the call time.</li>
 *   <li>CPU-bound work: virtual threads are NOT faster - cores still bound it.</li>
 *   <li>Resource limits: semaphores are still required (the key lesson).</li>
 * </ul>
 *
 * <p>Intentionally shown mistakes: unlimited concurrency without a Semaphore
 * ({@link VirtualThreadResourceLimitExample} phase C) - looks fast, destroys
 * downstream resources.
 *
 * <p>Key message: Virtual Threads remove the cost of waiting threads, NOT the
 * cost of the resources they are waiting for.
 */
package com.example.javalab.virtualthread;
