# Runs every java-lab example in sequence.
# Usage:  powershell -File scripts/run-all.ps1
# Requires: java 21+ on PATH, project compiled (mvn clean compile).

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$classpath = Join-Path $root 'target\classes'

# Prefer JAVA_HOME (e.g. a JDK 21 install); fall back to java on PATH.
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $java = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    $java = 'java'
}
Write-Host "Using: $java" -ForegroundColor DarkGray

if (-not (Test-Path $classpath)) {
    Write-Host 'Compiling first...' -ForegroundColor Yellow
    Push-Location $root
    mvn -q clean compile
    Pop-Location
}

$classes = @(
    'com.example.javalab.basics.CreateThreadExample',
    'com.example.javalab.basics.RunnableExample',
    'com.example.javalab.basics.StartVsRunExample',
    'com.example.javalab.basics.ThreadLifecycleExample',
    'com.example.javalab.synchronization.RaceConditionExample',
    'com.example.javalab.synchronization.SynchronizedExample',
    'com.example.javalab.synchronization.VolatileExample',
    'com.example.javalab.synchronization.AtomicIntegerExample',
    'com.example.javalab.synchronization.LockExample',
    'com.example.javalab.threadpool.FixedThreadPoolExample',
    'com.example.javalab.threadpool.ThreadPoolExecutorExample',
    'com.example.javalab.threadpool.BoundedQueueExample',
    'com.example.javalab.threadpool.ThreadPoolExhaustionExample',
    'com.example.javalab.threadpool.RejectedExecutionExample',
    'com.example.javalab.problems.DeadlockExample',
    'com.example.javalab.problems.StarvationExample',
    'com.example.javalab.problems.ThreadLocalLeakExample',
    'com.example.javalab.problems.LostExceptionExample',
    'com.example.javalab.problems.BlockingSharedPoolExample',
    'com.example.javalab.performance.CpuBoundThreadExample',
    'com.example.javalab.performance.IoBoundThreadExample',
    'com.example.javalab.performance.TooManyThreadsExample',
    'com.example.javalab.virtualthread.BasicVirtualThreadExample',
    'com.example.javalab.virtualthread.VirtualThreadExecutorExample',
    'com.example.javalab.virtualthread.PlatformVsVirtualThreadExample',
    'com.example.javalab.virtualthread.VirtualThreadIoExample',
    'com.example.javalab.virtualthread.VirtualThreadCpuBoundExample',
    'com.example.javalab.virtualthread.VirtualThreadResourceLimitExample',
    'com.example.javalab.practical.SemaphoreConcurrencyLimitExample',
    'com.example.javalab.practical.ProducerConsumerExample',
    'com.example.javalab.practical.GracefulShutdownExample'
)

$failed = 0
foreach ($cls in $classes) {
    Write-Host ''
    Write-Host ('===== ' + $cls + ' =====') -ForegroundColor Cyan
    & $java -cp $classpath $cls
    if ($LASTEXITCODE -ne 0) {
        Write-Host ('FAILED: ' + $cls + ' (exit ' + $LASTEXITCODE + ')') -ForegroundColor Red
        $failed++
    }
}

Write-Host ''
if ($failed -gt 0) {
    Write-Host ("$failed example(s) FAILED.") -ForegroundColor Red
    exit 1
}
Write-Host 'All examples completed successfully.' -ForegroundColor Green
