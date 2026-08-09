/**
 * Thread pools and ExecutorService.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>Fixed pools ({@code Executors.newFixedThreadPool}) and thread reuse.</li>
 *   <li>{@link java.util.concurrent.ThreadPoolExecutor} knobs: corePoolSize,
 *       maximumPoolSize, bounded queue, keepAliveTime, rejection policy.</li>
 *   <li>The submission pipeline: core threads -> queue -> max threads -> rejection.</li>
 *   <li>Unbounded vs bounded queues (max size is dead config with unbounded queue).</li>
 *   <li>Pool exhaustion: all workers blocked, queue growing, latency exploding.</li>
 *   <li>Rejection policies: Abort, CallerRuns, Discard, DiscardOldest.</li>
 * </ul>
 *
 * <p>Intentionally shown mistakes:
 * <ul>
 *   <li>{@link ThreadPoolExhaustionExample} - unbounded queue + fully blocked workers.</li>
 *   <li>{@link BoundedQueueExample} - "maximumPoolSize will save me" misconception.</li>
 * </ul>
 *
 * <p>In real applications: always bound the queue, pick a rejection policy that
 * produces backpressure, size core threads by workload type, and monitor
 * queue depth and pool size.
 */
package com.example.javalab.threadpool;
