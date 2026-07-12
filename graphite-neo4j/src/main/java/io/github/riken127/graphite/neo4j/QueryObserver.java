package io.github.riken127.graphite.neo4j;

/** Starts metrics, tracing, or structured logging observations around query execution. */
@FunctionalInterface
public interface QueryObserver {

  /** Starts an observation for a query execution attempt. */
  QueryObservation begin(QueryDescriptor descriptor);

  /** Returns an observer that performs no work. */
  static QueryObserver noop() {
    return ignored -> new QueryObservation() {};
  }
}
