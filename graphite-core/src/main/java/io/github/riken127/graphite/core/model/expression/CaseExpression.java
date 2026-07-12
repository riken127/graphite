package io.github.riken127.graphite.core.model.expression;

import java.util.List;
import java.util.Objects;

/** Typed searched CASE expression. */
public record CaseExpression<T>(
    List<CaseAlternative<T>> alternatives, Expression<? extends T> otherwise, Class<T> valueType)
    implements Expression<T> {

  /** Creates a validated CASE expression. */
  public CaseExpression {
    alternatives =
        List.copyOf(Objects.requireNonNull(alternatives, "alternatives must not be null"));
    if (alternatives.isEmpty()) {
      throw new IllegalArgumentException("alternatives must not be empty");
    }
    alternatives.forEach(value -> Objects.requireNonNull(value, "alternative must not be null"));
    Objects.requireNonNull(otherwise, "otherwise must not be null");
    Objects.requireNonNull(valueType, "valueType must not be null");
  }
}
