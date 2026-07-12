package io.github.riken127.graphite.neo4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;

final class DriverSummaryMapper {

  private DriverSummaryMapper() {}

  static QuerySummary map(ResultSummary summary) {
    SummaryCounters counters = summary.counters();
    return new QuerySummary(
        summary.queryType().name(),
        new QueryCounters(
            counters.containsUpdates(),
            counters.nodesCreated(),
            counters.nodesDeleted(),
            counters.relationshipsCreated(),
            counters.relationshipsDeleted(),
            counters.propertiesSet(),
            counters.labelsAdded(),
            counters.labelsRemoved(),
            counters.indexesAdded(),
            counters.indexesRemoved(),
            counters.constraintsAdded(),
            counters.constraintsRemoved(),
            counters.containsSystemUpdates(),
            counters.systemUpdates()),
        Duration.ofNanos(summary.resultAvailableAfter(TimeUnit.NANOSECONDS)),
        Duration.ofNanos(summary.resultConsumedAfter(TimeUnit.NANOSECONDS)),
        summary.database().name(),
        summary.server().address(),
        summary.server().agent(),
        summary.server().protocolVersion());
  }
}
