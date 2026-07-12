package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.model.expression.Expression;
import java.util.Objects;

/** Null test applied to an arbitrary expression. */
public record ExpressionNullPredicate(Expression<?> expression, boolean isNull)
    implements Predicate {

  /** Creates a validated expression null predicate. */
  public ExpressionNullPredicate {
    Objects.requireNonNull(expression, "expression must not be null");
  }
}
