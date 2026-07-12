package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.model.expression.Expression;
import java.util.Objects;

/** Comparison between two typed expressions. */
public record ExpressionComparisonPredicate(
    Expression<?> left, ComparisonOperator operator, Expression<?> right) implements Predicate {

  /** Creates a validated expression comparison. */
  public ExpressionComparisonPredicate {
    Objects.requireNonNull(left, "left must not be null");
    Objects.requireNonNull(operator, "operator must not be null");
    Objects.requireNonNull(right, "right must not be null");
  }
}
