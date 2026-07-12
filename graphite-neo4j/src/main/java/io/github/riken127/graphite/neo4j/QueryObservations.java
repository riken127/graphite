package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Locale;

final class QueryObservations {

  private static final QueryObservation NOOP = new QueryObservation() {};

  private QueryObservations() {}

  static QueryDescriptor descriptor(RenderedQuery query, boolean streaming) {
    String text = query.cypher().stripLeading();
    int separator = text.indexOf(' ');
    String operation = separator < 0 ? text : text.substring(0, separator);
    return new QueryDescriptor(
        operation.toUpperCase(Locale.ROOT), query.parameters().keySet(), streaming);
  }

  static QueryObservation begin(QueryObserver observer, QueryDescriptor descriptor) {
    try {
      QueryObservation observation = observer.begin(descriptor);
      return observation == null ? NOOP : observation;
    } catch (RuntimeException ignored) {
      return NOOP;
    }
  }

  static void succeeded(QueryObservation observation, QuerySummary summary, long recordCount) {
    try {
      observation.succeeded(summary, recordCount);
    } catch (RuntimeException ignored) {
      // Observability must not change database behavior.
    }
  }

  static void failed(QueryObservation observation, RuntimeException failure) {
    try {
      observation.failed(failure);
    } catch (RuntimeException ignored) {
      // Observability must not hide the database failure.
    }
  }

  static void cancelled(QueryObservation observation, long recordCount) {
    try {
      observation.cancelled(recordCount);
    } catch (RuntimeException ignored) {
      // Observability must not make resource cleanup fail.
    }
  }
}
