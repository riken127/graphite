package io.github.riken127.graphite.core.model.expression;

import java.util.List;
import java.util.Objects;

/** Typed list literal whose elements remain expressions. */
public record ListExpression<T>(List<Expression<? extends T>> elements, Class<T> elementType)
    implements Expression<List<T>> {

  /** Creates a validated list expression. */
  public ListExpression {
    elements = List.copyOf(Objects.requireNonNull(elements, "elements must not be null"));
    elements.forEach(value -> Objects.requireNonNull(value, "element must not be null"));
    Objects.requireNonNull(elementType, "elementType must not be null");
  }

  /** Returns the erased list result type. */
  @Override
  @SuppressWarnings("unchecked")
  public Class<List<T>> valueType() {
    return (Class<List<T>>) (Class<?>) List.class;
  }
}
