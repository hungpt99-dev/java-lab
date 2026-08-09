package com.example.javalab.practical;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The classic producer/consumer pattern with a BOUNDED {@link BlockingQueue}.
 *
 * <p>The queue has capacity 5. Two producers create 25 items each; three
 * consumers process them. When the queue is full, producers BLOCK - that is
 * backpressure: the slow side (consumers) forces the fast side (producers)
 * to slow down instead of overflowing memory.
 *
 * <p>Termination: each producer sends a "poison pill" after its items;
 * consumers exit when they see it. Everything shuts down cleanly.
 */
public class ProducerConsumerExample {

    private static final int QUEUE_CAPACITY = 5;
    private static final int ITEMS_PER_PRODUCER = 25;
    private static final int PRODUCERS = 2;
    private static final int CONSUMERS = 3;

    private static final String POISON = "POISON";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Producer / Consumer Example ===");
        System.out.println();
        System.out.println("Queue capacity: " + QUEUE_CAPACITY + " (bounded!)");
        System.out.println(PRODUCERS + " producers x " + ITEMS_PER_PRODUCER
                + " items, " + CONSUMERS + " consumers");
        System.out.println();

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        AtomicInteger produced = new AtomicInteger();
        AtomicInteger consumed = new AtomicInteger();
        CountDownLatch allConsumersDone = new CountDownLatch(CONSUMERS);

        // Producers
        for (int p = 1; p <= PRODUCERS; p++) {
            final int producerId = p;
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 1; i <= ITEMS_PER_PRODUCER; i++) {
                        queue.put("item-" + producerId + "-" + i);   // BLOCKS when full
                        produced.incrementAndGet();
                    }
                    // One poison pill per consumer so EVERY consumer terminates.
                    for (int c = 0; c < CONSUMERS; c++) {
                        queue.put(POISON);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "producer-" + p);
            producer.setDaemon(true);   // consumers do the termination bookkeeping
            producer.start();
        }

        // Consumers
        for (int c = 1; c <= CONSUMERS; c++) {
            final int consumerId = c;
            Thread consumer = new Thread(() -> {
                try {
                    while (true) {
                        String item = queue.take();                   // BLOCKS when empty
                        if (item.equals(POISON)) {
                            System.out.println("  [consumer-" + consumerId
                                    + "] got poison pill, exiting");
                            break;
                        }
                        consumed.incrementAndGet();
                        Thread.sleep(10);                             // simulated processing
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allConsumersDone.countDown();
                }
            }, "consumer-" + c);
            consumer.start();
        }

        // Wait for consumers to finish (they exit on poison pills).
        allConsumersDone.await(30, TimeUnit.SECONDS);
        Thread.sleep(300);   // let producers notice the queue drained

        System.out.println();
        System.out.println("produced=" + produced.get() + " consumed=" + consumed.get()
                + " (items may be in flight - the point is the pattern, not exact counts)");
        System.out.println();
        System.out.println("Observation:");
        System.out.println("- When the queue is FULL, producers block (backpressure).");
        System.out.println("- When it is EMPTY, consumers block (no busy-waiting).");
        System.out.println("- Bounded queue = bounded memory, whatever the producer speed.");
        System.out.println("In production this pattern is used for: task pipelines,");
        System.out.println("log draining, batch jobs, and rate-limiting between services.");
    }
}
