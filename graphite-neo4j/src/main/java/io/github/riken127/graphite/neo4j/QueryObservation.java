package io.github.riken127.graphite.neo4j;

/** Lifecycle callbacks for one physical query execution attempt. */
public interface QueryObservation {

  /** Reports successful consumption of a query result. */
  default void succeeded(QuerySummary summary, long recordCount) {}

  /** Reports query failure. */
  default void failed(RuntimeException failure) {}

  /** Reports early cancellation of a streaming query. */
  default void cancelled(long recordCount) {}
}
