/**
 * Practical concurrency control patterns for production code.
 *
 * <p>Demonstrated concepts:
 * <ul>
 *   <li>{@link java.util.concurrent.Semaphore}: cap concurrency to a scarce
 *       downstream resource (DB pool, API quota, Redis).</li>
 *   <li>Producer/consumer with a bounded {@link java.util.concurrent.BlockingQueue}:
 *       backpressure and clean termination via poison pills.</li>
 *   <li>Graceful shutdown of executors:
 *       shutdown() -> awaitTermination() -> shutdownNow().</li>
 * </ul>
 *
 * <p>These are the patterns that keep real applications stable: bounded
 * resources, bounded memory, and clean lifecycle management.
 */
package com.example.javalab.practical;
