package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.PathPattern;
import java.util.List;
import java.util.Objects;

/** CREATE clause containing one or more graph patterns. */
public record CreateClause(List<PathPattern> patterns) implements Clause {

  /** Creates a validated CREATE clause. */
  public CreateClause {
    patterns = List.copyOf(Objects.requireNonNull(patterns, "patterns must not be null"));
    if (patterns.isEmpty()) {
      throw new IllegalArgumentException("patterns must not be empty");
    }
    patterns.forEach(pattern -> Objects.requireNonNull(pattern, "pattern must not be null"));
  }
}
