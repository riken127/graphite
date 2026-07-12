package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.model.expression.Expression;
import java.util.Collection;
import java.util.Objects;

/** Membership predicate between a value expression and a collection expression. */
public record ExpressionInPredicate(
    Expression<?> value, Expression<? extends Collection<?>> collection) implements Predicate {

  /** Creates a validated expression membership predicate. */
  public ExpressionInPredicate {
    Objects.requireNonNull(value, "value must not be null");
    Objects.requireNonNull(collection, "collection must not be null");
  }
}
