package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.model.expression.Expression;
import java.util.Objects;

/** Text comparison between two typed expressions. */
public record ExpressionTextPredicate(
    Expression<String> value, TextOperator operator, Expression<String> expected)
    implements Predicate {

  /** Creates a validated typed text predicate. */
  public ExpressionTextPredicate {
    Objects.requireNonNull(value, "value must not be null");
    Objects.requireNonNull(operator, "operator must not be null");
    Objects.requireNonNull(expected, "expected must not be null");
  }
}
