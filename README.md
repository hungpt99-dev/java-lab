# java-lab

Runnable, educational examples about **Java Threads, Concurrency, Thread Pools,
Performance, Virtual Threads, common production mistakes - and JVM internals**.

This repository accompanies a technical blog series on Java. Every
example is a small, independent, runnable program that demonstrates exactly one
concept - including the mistakes, so you can see them fail and understand why.

No frameworks. No Spring. Pure JDK APIs (Java 21+).

## Project Overview

Topics covered:

| Topic | Package | What you will learn |
| ----- | ------- | ------------------- |
| Thread Basics | `basics` | Creating threads, Runnable/Callable, `start()` vs `run()`, lifecycle states |
| Synchronization | `synchronization` | Race conditions, `synchronized`, `volatile`, `AtomicInteger`, `ReentrantLock` |
| Thread Pools | `threadpool` | Fixed pools, `ThreadPoolExecutor`, bounded queues, rejection policies, exhaustion |
| Common Problems | `problems` | Deadlock, starvation, ThreadLocal leaks, lost exceptions, blocking shared pools |
| Performance | `performance` | CPU-bound vs I/O-bound, thread count vs wall time |
| Virtual Threads | `virtualthread` | Java 21 virtual threads, blocking I/O, CPU-bound limits, resource limits |
| Practical Control | `practical` | Semaphores, producer/consumer, graceful shutdown |
| JVM Internals | `jvminternals` | Bytecode, class loading, stack frames, references, GC reachability, generational GC, memory leaks, OOME flavors, escape analysis, boxed-type traps |

## Prerequisites

- **Java 21 or newer** (Virtual Threads require 21+; `mvn --version` must show a 21+ runtime)

  ```bash
  java -version
  # openjdk version "21" ... or newer
  ```

- **Maven 3.6+**

  ```bash
  mvn -version
  ```

## How to Run

Compile everything:

```bash
mvn clean compile
```

Run a single example with Maven:

```bash
mvn compile exec:java -Dexec.mainClass=com.example.javalab.basics.StartVsRunExample
```

Or run directly against the compiled classes (faster):

```bash
mvn clean compile
java -cp target/classes com.example.javalab.basics.StartVsRunExample
```

Run every example in sequence (PowerShell):

```bash
powershell -File scripts/run-all.ps1
```

### All example entry points

| # | Example | Command (after `mvn clean compile`) |
| - | ------- | ----------------------------------- |
| 1 | Create a Thread | `java -cp target/classes com.example.javalab.basics.CreateThreadExample` |
| 2 | Runnable vs Callable | `java -cp target/classes com.example.javalab.basics.RunnableExample` |
| 3 | start() vs run() | `java -cp target/classes com.example.javalab.basics.StartVsRunExample` |
| 4 | Thread lifecycle | `java -cp target/classes com.example.javalab.basics.ThreadLifecycleExample` |
| 5 | Race condition (broken) | `java -cp target/classes com.example.javalab.synchronization.RaceConditionExample` |
| 6 | synchronized | `java -cp target/classes com.example.javalab.synchronization.SynchronizedExample` |
| 7 | volatile | `java -cp target/classes com.example.javalab.synchronization.VolatileExample` |
| 8 | AtomicInteger | `java -cp target/classes com.example.javalab.synchronization.AtomicIntegerExample` |
| 9 | ReentrantLock | `java -cp target/classes com.example.javalab.synchronization.LockExample` |
| 10 | Fixed thread pool | `java -cp target/classes com.example.javalab.threadpool.FixedThreadPoolExample` |
| 11 | ThreadPoolExecutor | `java -cp target/classes com.example.javalab.threadpool.ThreadPoolExecutorExample` |
| 12 | Bounded vs unbounded queue | `java -cp target/classes com.example.javalab.threadpool.BoundedQueueExample` |
| 13 | Pool exhaustion | `java -cp target/classes com.example.javalab.threadpool.ThreadPoolExhaustionExample` |
| 14 | Rejection policies | `java -cp target/classes com.example.javalab.threadpool.RejectedExecutionExample` |
| 15 | Deadlock | `java -cp target/classes com.example.javalab.problems.DeadlockExample` |
| 16 | Starvation | `java -cp target/classes com.example.javalab.problems.StarvationExample` |
| 17 | ThreadLocal leak | `java -cp target/classes com.example.javalab.problems.ThreadLocalLeakExample` |
| 18 | Lost exceptions | `java -cp target/classes com.example.javalab.problems.LostExceptionExample` |
| 19 | Blocking shared pool | `java -cp target/classes com.example.javalab.problems.BlockingSharedPoolExample` |
| 20 | CPU-bound benchmark | `java -cp target/classes com.example.javalab.performance.CpuBoundThreadExample` |
| 21 | I/O-bound benchmark | `java -cp target/classes com.example.javalab.performance.IoBoundThreadExample` |
| 22 | Too many threads | `java -cp target/classes com.example.javalab.performance.TooManyThreadsExample` |
| 23 | Virtual threads basics | `java -cp target/classes com.example.javalab.virtualthread.BasicVirtualThreadExample` |
| 24 | VT executor | `java -cp target/classes com.example.javalab.virtualthread.VirtualThreadExecutorExample` |
| 25 | Platform vs virtual | `java -cp target/classes com.example.javalab.virtualthread.PlatformVsVirtualThreadExample` |
| 26 | VTs for blocking I/O | `java -cp target/classes com.example.javalab.virtualthread.VirtualThreadIoExample` |
| 27 | VTs and CPU-bound | `java -cp target/classes com.example.javalab.virtualthread.VirtualThreadCpuBoundExample` |
| 28 | VT resource limits | `java -cp target/classes com.example.javalab.virtualthread.VirtualThreadResourceLimitExample` |
| 29 | Semaphore limit | `java -cp target/classes com.example.javalab.practical.SemaphoreConcurrencyLimitExample` |
| 30 | Producer/consumer | `java -cp target/classes com.example.javalab.practical.ProducerConsumerExample` |
| 31 | Graceful shutdown | `java -cp target/classes com.example.javalab.practical.GracefulShutdownExample` |

### JVM Internals example entry points

| # | Example | Command (after `mvn clean compile`) |
| - | ------- | ----------------------------------- |
| 32 | Bytecode of `new User(...)` | `java -cp target/classes com.example.javalab.jvminternals.BytecodeExample` |
| 33 | Class loader hierarchy | `java -cp target/classes com.example.javalab.jvminternals.ClassLoaderHierarchyExample` |
| 34 | JIT warm-up | `java -cp target/classes com.example.javalab.jvminternals.WarmUpExample` |
| 35 | Stack overflow / `-Xss` | `java -cp target/classes com.example.javalab.jvminternals.StackOverflowExample` |
| 36 | Reference vs object | `java -cp target/classes com.example.javalab.jvminternals.ObjectReferenceExample` |
| 37 | Field init order | `java -cp target/classes com.example.javalab.jvminternals.FieldInitializationExample` |
| 38 | GC reachability | `java -cp target/classes com.example.javalab.jvminternals.ReachabilityExample` |
| 39 | Circular references | `java -cp target/classes com.example.javalab.jvminternals.CircularReferenceExample` |
| 40 | Generational GC | `java -cp target/classes com.example.javalab.jvminternals.GenerationalGcExample` |
| 41 | Static cache leak | `java -cp target/classes com.example.javalab.jvminternals.MemoryLeakExample` |
| 42 | OOME areas | `java -cp target/classes com.example.javalab.jvminternals.OutOfMemoryAreasExample heap` |
| 43 | Escape analysis | `java -cp target/classes com.example.javalab.jvminternals.EscapeAnalysisExample` |
| 44 | Integer cache | `java -cp target/classes com.example.javalab.jvminternals.IntegerCacheExample` |
| 45 | Null unboxing NPE | `java -cp target/classes com.example.javalab.jvminternals.UnboxingNpeExample` |
| 46 | String pool | `java -cp target/classes com.example.javalab.jvminternals.StringPoolExample` |
| 47 | finally-return trap | `java -cp target/classes com.example.javalab.jvminternals.FinallyReturnExample` |

## Learning Path

Study the examples in this order - each step builds on the previous one:

1. **Thread Basics** (`basics`) - what a thread is, how to create one,
   `start()` vs `run()`, the six lifecycle states.
2. **Synchronization** (`synchronization`) - why shared state breaks, and the
   three fixes: `synchronized`, `Atomic*`, locks. What `volatile` does and
   does not do.
3. **Thread Pools** (`threadpool`) - how `ThreadPoolExecutor` really works:
   core threads -> queue -> max threads -> rejection. Bounded queues,
   backpressure, exhaustion.
4. **Common Problems** (`problems`) - deadlock, starvation, ThreadLocal leaks,
   lost exceptions, blocking shared pools - the incidents that happen in
   production.
5. **Performance** (`performance`) - the CPU-bound vs I/O-bound distinction,
   and why "more threads" is not "more speed".
6. **Virtual Threads** (`virtualthread`) - Java 21's answer to blocking
   concurrency - and its honest limits.
7. **Practical Concurrency Control** (`practical`) - semaphores, producer/
   consumer, graceful shutdown: the patterns that keep real systems stable.

## JVM Internals experiments

The `jvminternals` package is a companion to the "How Java Actually Works"
blog post. Each example answers one question about the runtime underneath the
language. Several experiments only show their full effect with JVM flags or
`javap` - the exact commands are below.

| # | Question | How to observe it |
| - | -------- | ----------------- |
| 32 | What does `new User("Hung")` compile to? | Run `BytecodeExample`, then `javap -c -p target/classes/com/example/javalab/jvminternals/BytecodeExample.class` - find `new / dup / ldc / invokespecial / astore_1` and `iload / iadd`. |
| 33 | Who loads my classes, and when? | `java -Xlog:class+load -cp target/classes com.example.javalab.jvminternals.ClassLoaderHierarchyExample` - the entry point loads immediately, `Lazy` only at first use. |
| 34 | Does warm-up really make Java faster? | `java -Xlog:jit+compilation -cp target/classes com.example.javalab.jvminternals.WarmUpExample` - later epochs print lower ns/call; compilation events appear mid-run. (`-Xlog:jit+compilation` is the JDK 24+ tag; use `-Xlog:compilation` on JDK 21-23.) |
| 35 | Why does recursion fail on big inputs? | `java -Xss256k -cp ... StackOverflowExample` vs `java -Xss4m -cp ... StackOverflowExample` - the frame depth scales with the stack size. |
| 36 | Is a local variable the object itself? | `java -cp target/classes ... ObjectReferenceExample` - two variables, one identity; nulling one handle leaves the object alive. |
| 37 | In what order are fields initialized? | `java -cp target/classes ... FieldInitializationExample` - super() -> field initializers -> constructor body; unassigned fields read defaults. |
| 38 | Does `user = null` delete anything? | `java -cp target/classes ... ReachabilityExample` - the object survives until the GC reclaims it as unreachable. |
| 39 | Do circular references leak? | `java -cp target/classes ... CircularReferenceExample` - both objects of an unreachable A <-> B cycle are collected. |
| 40 | Why is young GC cheap? | `java -Xmx256m -Xlog:gc -cp ... GenerationalGcExample` - young-gen collection count rises, old-gen stays flat. |
| 41 | What is a Java memory leak, really? | `java -cp target/classes ... MemoryLeakExample` - a static map keeps every entry; a WeakHashMap with the same data sheds it. |
| 42 | Is OOME always the heap? | `java -Xmx32m -cp ... OutOfMemoryAreasExample heap` and `java -XX:MaxDirectMemorySize=8m -cp ... OutOfMemoryAreasExample direct` - two different messages, two different knobs. |
| 43 | Can the JVM skip allocations I can see in the source? | `java -Xmx64m -Xlog:gc -cp ... EscapeAnalysisExample` vs the same with `-XX:-DoEscapeAnalysis` - GC pauses appear only when escape analysis is off. |
| 44 | Why is `a == b` true for 100 and false for 200? | `java -cp target/classes ... IntegerCacheExample`; try `-XX:AutoBoxCacheMax=2000` to extend the cached range. |
| 45 | Can unboxing a null really throw? | `java -cp target/classes ... UnboxingNpeExample` - prints the JDK 14+ helpful message naming `intValue()`. |
| 46 | What does `==` on strings actually compare? | `java -cp target/classes ... StringPoolExample` - literals are interned, `new String()` and runtime concatenation are not. |
| 47 | Can `finally` change a return value? | `java -cp target/classes ... FinallyReturnExample`; `javap -c -p ...` shows the finally body duplicated on every exit path. |

Conventions for the JVM internals examples:

- **Observable, not asserted.** Each run ends with an `Observation:` block
  that states what you should have seen and why it happens.
- **Honest about guarantees.** The README text and the examples' javadoc
  distinguish JLS guarantees (field defaults, initialization order) from
  JVM-spec behavior from common HotSpot behavior (TLABs, `-Xss` defaults,
  escape analysis) - only the first is guaranteed to stay true.
- **Bounded experiments.** GC-churn loops, OOM demos and recursion all have
  safety caps so every example terminates quickly even with default flags;
  the flags above are for the "textbook" run.

## Key Lessons

Everything in this repo is built around these principles:

- **A Thread does not automatically make code faster.** It only gives work a
  chance to run in parallel - beyond the machine's cores it is pure overhead.
- **Concurrency is not the same as parallelism.** Concurrency is structure
  (interleaving); parallelism is execution (simultaneous cores).
- **More threads do not mean more CPU power.** CPU-bound work is bounded by
  cores; extra threads buy context switches.
- **Thread pools need bounded resources and backpressure.** Unbounded queues
  and missing rejection policies turn a slow consumer into an OOM.
- **Shared mutable state is dangerous.** Every race in this repo is the same
  failure: unsynchronized read-modify-write.
- **Virtual Threads do not make code automatically thread-safe.** All the
  synchronization rules still apply unchanged.
- **Virtual Threads are best suited for high-concurrency blocking I/O
  workloads** - HTTP calls, database calls, file I/O.
- **Resource limits still exist even when Virtual Threads are cheap.** The
  database connection pool, API quota, and Redis slot counts do not change.

The central message:

> Virtual Threads remove the cost of waiting threads - not the cost of the
> resources they are waiting for.

## Intentionally Nondeterministic Results

These examples deliberately depend on scheduling, JIT state and machine speed.
Do not expect exact numbers:

- `RaceConditionExample` / `VolatileExample`: whether a race or visibility bug
  shows in a given trial varies (more cores/load = more likely).
- `CpuBoundThreadExample`, `IoBoundThreadExample`, `TooManyThreadsExample`,
  `VirtualThreadCpuBoundExample`: wall times and speedups are machine-specific;
  watch the TREND, not the numbers.
- `ThreadLifecycleExample`: the exact state observed at each poll is timing-
  dependent (it polls until the expected state appears).
- `ProducerConsumerExample`: final produced/consumed counts can differ by a few
  in-flight items.
- `ThreadPoolExhaustionExample` / `StarvationExample`: printed latencies are
  approximate.
- `WarmUpExample` / `EscapeAnalysisExample`: ns/call numbers and GC pause
  counts are JIT- and machine-dependent; the OBSERVATION is the trend (later
  epochs faster, no pauses with escape analysis on).
- `ReachabilityExample` / `CircularReferenceExample` / `MemoryLeakExample`:
  how many MB of churn it takes to clear a weak reference varies per run and
  per collector; whether it IS cleared is deterministic.
- `GenerationalGcExample`: collection counts depend on heap size; the
  relationship (young moves, old stays flat) does not.

Deterministic by design: every example terminates cleanly, executors are shut
down, and dangerous examples (`DeadlockExample`) use daemon threads so the JVM
always exits.

## Safety Notes

- `DeadlockExample` intentionally deadlocks two **daemon** threads and then
  shuts down - inspect it with `jcmd <pid> Thread.print` while it runs.
- `TooManyThreadsExample` is bounded by task counts and pool sizes; it never
  attempts to crash the machine.
- `OutOfMemoryAreasExample` catches every `OutOfMemoryError` it provokes and
  keeps a reserve so the catch handler itself never starves; run it with the
  documented `-Xmx32m` / `-XX:MaxDirectMemorySize=8m` flags for the real
  effect (default runs stop at a 2 GB safety budget).
- `StackOverflowExample` catches the `StackOverflowError` and exits normally;
  only the frame depth varies with `-Xss`.
- `ReachabilityExample`, `CircularReferenceExample`, `MemoryLeakExample`,
  `GenerationalGcExample`: allocation churn is capped (2-4 GB worst case,
  typically tens of MB).
- No example performs real external I/O; all "network" and "database" calls are
  simulated with `Thread.sleep` / `LockSupport.parkNanos`.

## Project Structure

```
java-lab/
├── README.md
├── pom.xml
├── docs/
│   └── architecture.md          # design notes, conventions, debugging tips
├── scripts/
│   └── run-all.ps1              # run all 47 examples in sequence
└── src/main/java/com/example/javalab/
    ├── basics/                  # threads, Runnable/Callable, lifecycle
    ├── synchronization/         # races, synchronized, volatile, Atomic*, locks
    ├── threadpool/              # ExecutorService, ThreadPoolExecutor, policies
    ├── problems/                # deadlock, starvation, leaks, lost exceptions
    ├── performance/             # CPU-bound vs I/O-bound, thread-count experiments
    ├── virtualthread/           # Java 21 virtual threads
    ├── practical/               # semaphores, producer/consumer, shutdown
    └── jvminternals/            # bytecode, class loading, GC, escape analysis
```

Each package has a `package-info.java` documenting the concepts, the mistakes
shown, what to observe, and when to use the technique in real applications.
See `docs/architecture.md` for the full design rationale.
