package com.example.javalab.jvminternals;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows that {@code OutOfMemoryError} is a hat for many heads: the heap is
 * only ONE of the regions that can fail. The example can reproduce two of
 * them safely:
 *
 * <pre>
 *   # 1) Java heap space   - bounds the heap with -Xmx
 *   java -Xmx32m -cp target/classes com.example.javalab.jvminternals.OutOfMemoryAreasExample heap
 *
 *   # 2) Direct buffer memory - bounds off-heap buffers
 *   java -XX:MaxDirectMemorySize=8m -cp target/classes com.example.javalab.jvminternals.OutOfMemoryAreasExample direct
 * </pre>
 *
 * <p>Metaspace (class metadata) and native threads fail via different
 * mechanisms; the practical point is identical: read the EXCEPTION MESSAGE -
 * it names the subsystem, and every subsystem has its own knob.
 *
 * <p>Safe by design: allocations are bounded so a default run cannot exhaust
 * the machine; every {@code OutOfMemoryError} is caught and reported.
 */
public class OutOfMemoryAreasExample {

    private static final int MAX_CHUNKS = 2048;      // safety budget for default runs
    private static final int CHUNK_SIZE = 1024 * 1024;

    public static void main(String[] args) {
        System.out.println("=== OutOfMemoryError Areas Example ===");
        System.out.println();
        System.out.println("VM input args: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
        System.out.println();
        System.out.println("Managed memory regions right now:");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage u = pool.getUsage();
            System.out.printf("  %-28s used=%7s  max=%s%n",
                    pool.getName(), mb(u.getUsed()), u.getMax() < 0 ? "unbounded" : mb(u.getMax()) + " MB");
        }
        System.out.println();

        String mode = args.length > 0 ? args[0] : "heap";
        switch (mode) {
            case "heap" -> {
                System.out.println("Mode: heap.  For the real effect run with -Xmx32m.");
                leakHeap();
            }
            case "direct" -> {
                System.out.println("Mode: direct. For the real effect run with -XX:MaxDirectMemorySize=8m.");
                leakDirect();
            }
            default -> {
                System.out.println("Usage: OutOfMemoryAreasExample [heap|direct]");
                System.out.println("  heap   - exhaust the heap, catch OutOfMemoryError");
                System.out.println("  direct - exhaust direct buffers, catch OutOfMemoryError");
            }
        }
    }

    private static void leakHeap() {
        // Reserve heap so the catch handler has room to print its message.
        byte[] reserve = new byte[1024 * 1024];
        List<byte[]> chunks = new ArrayList<>();
        try {
            while (chunks.size() < MAX_CHUNKS) {
                chunks.add(new byte[CHUNK_SIZE]);
            }
            System.out.println("No OOM within the " + MAX_CHUNKS + " MB safety budget;");
            System.out.println("re-run with -Xmx32m to see 'OutOfMemoryError: Java heap space'.");
        } catch (OutOfMemoryError e) {
            reserve = null;                        // give the reserve back to the heap
            System.out.println("Caught: " + e);
            System.out.println("  reachable chunks kept: " + chunks.size() + " MB");
        }
        System.out.println("  knob for this region: -Xmx (only this region).");
    }

    private static void leakDirect() {
        List<ByteBuffer> buffers = new ArrayList<>();
        try {
            while (buffers.size() < MAX_CHUNKS) {
                buffers.add(ByteBuffer.allocateDirect(CHUNK_SIZE));
            }
            System.out.println("No OOM within the safety budget; re-run with");
            System.out.println("-XX:MaxDirectMemorySize=8m to see 'OutOfMemoryError: Direct buffer memory'.");
        } catch (OutOfMemoryError e) {
            System.out.println("Caught: " + e);
            System.out.println("  reachable direct buffers kept: " + buffers.size() + " MB");
        }
        System.out.println("  knob for this region: -XX:MaxDirectMemorySize (off-heap).");
    }

    private static String mb(long bytes) {
        return String.valueOf(bytes / (1024 * 1024));
    }
}