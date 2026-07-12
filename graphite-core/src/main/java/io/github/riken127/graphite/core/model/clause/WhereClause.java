package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.Objects;

/** WHERE filter clause. */
public record WhereClause(Predicate predicate) implements Clause {

  /** Creates a validated WHERE clause. */
  public WhereClause {
    Objects.requireNonNull(predicate, "predicate must not be null");
  }
}
