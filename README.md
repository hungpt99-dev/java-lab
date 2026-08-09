# java-lab

Runnable, educational examples about **Java Threads, Concurrency, Thread Pools,
Performance, Virtual Threads and common production mistakes**.

This repository accompanies a technical blog series on Java concurrency. Every
example is a small, independent, runnable program that demonstrates exactly one
concept - including the mistakes, so you can see them fail and understand why.

No frameworks. No Spring. Pure JDK concurrency APIs (Java 21+).

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

Deterministic by design: every example terminates cleanly, executors are shut
down, and dangerous examples (`DeadlockExample`) use daemon threads so the JVM
always exits.

## Safety Notes

- `DeadlockExample` intentionally deadlocks two **daemon** threads and then
  shuts down - inspect it with `jcmd <pid> Thread.print` while it runs.
- `TooManyThreadsExample` is bounded by task counts and pool sizes; it never
  attempts to crash the machine.
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
│   └── run-all.ps1              # run all 31 examples in sequence
└── src/main/java/com/example/javalab/
    ├── basics/                  # threads, Runnable/Callable, lifecycle
    ├── synchronization/         # races, synchronized, volatile, Atomic*, locks
    ├── threadpool/              # ExecutorService, ThreadPoolExecutor, policies
    ├── problems/                # deadlock, starvation, leaks, lost exceptions
    ├── performance/             # CPU-bound vs I/O-bound, thread-count experiments
    ├── virtualthread/           # Java 21 virtual threads
    └── practical/               # semaphores, producer/consumer, shutdown
```

Each package has a `package-info.java` documenting the concepts, the mistakes
shown, what to observe, and when to use the technique in real applications.
See `docs/architecture.md` for the full design rationale.
