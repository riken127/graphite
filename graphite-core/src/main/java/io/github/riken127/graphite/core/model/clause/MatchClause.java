package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.PathPattern;
import java.util.List;
import java.util.Objects;

/** MATCH or OPTIONAL MATCH containing one or more graph patterns. */
public record MatchClause(List<PathPattern> patterns, boolean optional) implements Clause {

  /** Creates a validated match clause. */
  public MatchClause {
    patterns = List.copyOf(Objects.requireNonNull(patterns, "patterns must not be null"));
    if (patterns.isEmpty()) {
      throw new IllegalArgumentException("patterns must not be empty");
    }
    patterns.forEach(pattern -> Objects.requireNonNull(pattern, "pattern must not be null"));
  }
}
