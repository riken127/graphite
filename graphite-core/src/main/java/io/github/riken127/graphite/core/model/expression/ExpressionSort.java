package io.github.riken127.graphite.core.model.expression;

import io.github.riken127.graphite.core.model.SortDirection;
import java.util.Objects;

/** Sort direction applied to an arbitrary expression. */
public record ExpressionSort(Expression<?> expression, SortDirection direction) {

  /** Creates a validated expression sort. */
  public ExpressionSort {
    Objects.requireNonNull(expression, "expression must not be null");
    Objects.requireNonNull(direction, "direction must not be null");
  }
}
