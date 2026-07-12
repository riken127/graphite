package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.model.clause.Clause;
import java.util.List;
import java.util.Objects;

/** General query model composed from an ordered sequence of typed clauses. */
public record ClauseQuery(List<Clause> clauses) implements Query {

  /** Creates an immutable clause query. */
  public ClauseQuery {
    clauses = List.copyOf(Objects.requireNonNull(clauses, "clauses must not be null"));
    if (clauses.isEmpty()) {
      throw new IllegalArgumentException("clauses must not be empty");
    }
    clauses.forEach(clause -> Objects.requireNonNull(clause, "clause must not be null"));
  }
}
