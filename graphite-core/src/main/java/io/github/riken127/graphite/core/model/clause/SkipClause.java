package io.github.riken127.graphite.core.model.clause;

/** SKIP row count. */
public record SkipClause(int rows) implements Clause {

  /** Creates a validated SKIP clause. */
  public SkipClause {
    if (rows < 0) {
      throw new IllegalArgumentException("rows must be non-negative");
    }
  }
}
