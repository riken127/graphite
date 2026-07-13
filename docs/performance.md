# Performance and benchmarking

Graphite prioritizes deterministic query construction, parameterization, and metadata caching. The
project does not publish benchmark numbers yet; no performance claim should be inferred from unit
test timings.

## Measurement policy

Performance results must include:

* Graphite commit or release, JDK distribution/version, JVM flags, CPU, memory, and operating system
* Neo4j and driver versions for database benchmarks
* warmup and measurement iteration counts, forks, concurrency, and dataset shape
* allocation data alongside throughput or latency where practical
* raw output and the exact command used

Microbenchmarks should use JMH and avoid dead-code elimination. Database benchmarks should separate
query construction/rendering, driver/network latency, and server execution. Comparisons must use
equivalent parameterization, transaction boundaries, result consumption, and database state.

## Initial benchmark targets

The first maintained suite should cover:

1. building and rendering representative read and write queries;
2. cold and cached metadata discovery;
3. mapping flat and nested immutable results;
4. buffered versus streaming Neo4j result consumption.

Until that suite is checked in and run in a controlled environment, performance regressions are
evaluated with focused profiling and reproducible issue-specific benchmarks.
