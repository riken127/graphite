package io.github.riken127.graphite.neo4j;

import java.time.Duration;
import java.util.Objects;

/** Stable execution summary independent of driver summary implementation classes. */
public record QuerySummary(
    String queryType,
    QueryCounters counters,
    Duration resultAvailableAfter,
    Duration resultConsumedAfter,
    String database,
    String serverAddress,
    String serverAgent,
    String protocolVersion) {

  /** Validates and creates an execution summary. */
  public QuerySummary {
    Objects.requireNonNull(queryType, "queryType must not be null");
    Objects.requireNonNull(counters, "counters must not be null");
    Objects.requireNonNull(resultAvailableAfter, "resultAvailableAfter must not be null");
    Objects.requireNonNull(resultConsumedAfter, "resultConsumedAfter must not be null");
    Objects.requireNonNull(database, "database must not be null");
    Objects.requireNonNull(serverAddress, "serverAddress must not be null");
    Objects.requireNonNull(serverAgent, "serverAgent must not be null");
    Objects.requireNonNull(protocolVersion, "protocolVersion must not be null");
  }
}
