package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.Expression;
import java.util.Objects;

/** Typed property assignment used by SET and MERGE update actions. */
public record SetAssignment<T>(Expression<T> target, Expression<? extends T> value) {

  /** Creates a validated assignment. */
  public SetAssignment {
    Objects.requireNonNull(target, "target must not be null");
    Objects.requireNonNull(value, "value must not be null");
  }
}
