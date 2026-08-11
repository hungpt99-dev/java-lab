# java-lab - Architecture and Design Notes

## 1. Purpose

`java-lab` is a companion repository for a technical blog series about Java
Threads, Concurrency, Thread Pools, Performance, Virtual Threads, common
production mistakes - and JVM internals. It is a **library of runnable
experiments**, not an application.

## 2. Design Principles

1. **One concept per example.** Each class demonstrates exactly one idea.
   The class name states the concept; the `main` method runs it; comments
   explain the mechanics; console output states the observation.

2. **Mistakes are shown, then fixed.** Broken examples (`RaceConditionExample`,
   `VolatileExample`, `ThreadLocalLeakExample`) are intentionally broken but
   always compile, always terminate, and end with the production-grade fix or
   prevention rules.

3. **No framework, minimal dependencies.** Everything uses standard JDK
   concurrency APIs (`java.util.concurrent`, `java.lang.management`). The
   `pom.xml` has zero runtime dependencies.

4. **Controlled danger.** `DeadlockExample` uses daemon threads so the JVM can
   always exit. Starvation/exhaustion examples are bounded by timers and
   latches. No example performs real external I/O - "network" and "database"
   calls are simulated with `Thread.sleep` / `LockSupport.parkNanos`.

5. **ASCII-only console output.** Console output avoids non-ASCII characters
   so it renders correctly on any terminal/encoding.

6. **Clean termination everywhere.** Executors are shut down in `finally` or
   try-with-resources; latches have timeouts; threads are joined.

## 3. Package Map

| Package | Blog section | Core lessons |
| ------- | ------------ | ------------ |
| `basics` | What is a Thread / creating threads | `start()` vs `run()`; six states |
| `synchronization` | Race conditions & fixes | atomicity/visibility/ordering; volatile limits |
| `threadpool` | Thread pools | submission pipeline; bounded queues; backpressure |
| `problems` | Common bugs | deadlock, starvation, leaks, hidden failures |
| `performance` | Thread performance | cores bound CPU; concurrency helps I/O |
| `virtualthread` | Virtual Threads | cheap blocking; CPU no faster; limits remain |
| `practical` | Production patterns | semaphores, producer/consumer, shutdown |
| `jvminternals` | How Java works internally | bytecode, class loading, references, GC reachability, escape analysis, boxed traps |

## 4. Conventions

- Package root: `com.example.javalab`
- Every example has `public static void main(String[] args)`.
- Output starts with `=== <Concept> Example ===` and ends with an `Observation:`
  section that states the takeaway in plain words.
- Examples with several phases print numbered phase headers.
- Shared mutable state in examples uses `Atomic*` or is clearly marked broken.
- `Thread.sleep` is used for simulation only, never for correctness reasoning.

## 5. Threading Sizing Constants

For the performance and virtual-thread experiments, sizes are driven by
`Runtime.getRuntime().availableProcessors()` so the same code behaves
sensibly on 2-core and 64-core machines. `TooManyThreadsExample` accepts
command-line overrides:

```bash
java -cp target/classes com.example.javalab.performance.TooManyThreadsExample 400 4 64 400 800
# tasks=400, thread counts = 4, 64, 400, 800
```

## 6. Debugging While an Example Runs

- **Thread dumps**: `jcmd <pid> Thread.print` (or `jstack <pid>`).
  `DeadlockExample` prints `findDeadlockedThreads()` results automatically;
  you can also attach `jcmd` while it sleeps.
- **States to look for**: BLOCKED piles = monitor contention; WAITING piles =
  queue/pool exhaustion; all workers busy on one call = slow dependency.
- **Executor metrics** (production): Micrometer gauges
  `executor_active_threads`, `executor_queue_size`,
  `executor_completed_task_count`; HikariCP `hikaricp_connections_pending`.

## 7. Virtual Threads Notes

- Requires Java 21+; compiled with `maven.compiler.release=21`.
- `newVirtualThreadPerTaskExecutor` is used with try-with-resources so
  `close()` waits for all tasks (the `ExecutorService.close()` behaviour).
- The resource-limit example is the centerpiece: it proves with numbers that
  throughput is bounded by the semaphore (the "database"), not by the thread
  type.

## 8. JVM Internals Notes

- **Accuracy first.** The `jvminternals` package must never present a HotSpot
  implementation detail as a language guarantee. The javadoc and README
  explicitly tag claims: "language guarantee" (field defaults, init order),
  "JVM spec" (reference semantics), "common HotSpot behavior" (TLABs, `-Xss`
  default, escape analysis, GC pause counts).
- **Bounded danger.** GC-churn experiments (`ReachabilityExample`,
  `CircularReferenceExample`, `MemoryLeakExample`, `GenerationalGcExample`)
  cap allocation at 2-4 GB worst case and typically finish in tens of MB.
  `OutOfMemoryAreasExample` catches its own OOMEs, keeps a heap reserve so
  the catch handler can print, and caps at a 2 GB budget; the documented
  `-Xmx32m` / `-XX:MaxDirectMemorySize=8m` runs are the "textbook" versions.
- **Flag-driven experiments.** Several experiments are meant to be run twice
  (default vs `-Xss256k`, default vs `-XX:-DoEscapeAnalysis`) or with logs
  (`-Xlog:gc`, `-Xlog:class+load`, `-Xlog:compilation`, `-Xlog:stringtable`).
  The README section "JVM Internals experiments" is the single source of
  truth for the exact commands.
- **`javap` is part of the lab.** `BytecodeExample` and
  `FinallyReturnExample` are designed to be read as bytecode; their javadoc
  tells the reader exactly which instructions to look for.

## 9. Validation

Before release: `mvn clean compile` must pass with Java 21; every example must
start, print its observation, and exit on its own (checked with
`scripts/run-all.ps1`, which fails loudly if a class times out or returns a
non-zero exit code).
