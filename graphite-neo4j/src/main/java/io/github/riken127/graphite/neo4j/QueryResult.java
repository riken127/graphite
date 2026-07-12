package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.neo4j.exception.GraphiteResultCardinalityException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Materialized query records, execution summary, and causal bookmarks. */
public record QueryResult<T>(List<T> records, QuerySummary summary, Set<String> bookmarks) {

  /** Creates an immutable materialized query result. */
  public QueryResult {
    records = List.copyOf(Objects.requireNonNull(records, "records must not be null"));
    Objects.requireNonNull(summary, "summary must not be null");
    bookmarks =
        Collections.unmodifiableSet(
            new LinkedHashSet<>(Objects.requireNonNull(bookmarks, "bookmarks must not be null")));
  }

  /** Returns the only record, failing if the result cardinality is not exactly one. */
  public T single() {
    if (records.size() != 1) {
      throw new GraphiteResultCardinalityException("one", records.size());
    }
    return records.getFirst();
  }

  /** Returns an optional record, failing if more than one record exists. */
  public Optional<T> optional() {
    if (records.size() > 1) {
      throw new GraphiteResultCardinalityException("zero or one", records.size());
    }
    return records.isEmpty() ? Optional.empty() : Optional.of(records.getFirst());
  }

  QueryResult<T> withBookmarks(Set<String> causalBookmarks) {
    return new QueryResult<>(records, summary, causalBookmarks);
  }
}
