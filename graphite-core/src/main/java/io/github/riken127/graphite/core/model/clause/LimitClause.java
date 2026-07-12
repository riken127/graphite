package io.github.riken127.graphite.core.model.clause;

/** LIMIT row count. */
public record LimitClause(int rows) implements Clause {

  /** Creates a validated LIMIT clause. */
  public LimitClause {
    if (rows <= 0) {
      throw new IllegalArgumentException("rows must be positive");
    }
  }
}
