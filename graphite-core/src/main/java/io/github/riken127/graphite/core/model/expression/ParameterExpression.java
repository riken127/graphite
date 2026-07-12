package io.github.riken127.graphite.core.model.expression;

import java.util.Objects;

/** Parameterized literal value in an expression tree. */
public record ParameterExpression<T>(T value, Class<T> valueType) implements Expression<T> {

  /** Creates a non-null parameter expression. */
  public ParameterExpression {
    Objects.requireNonNull(value, "value must not be null");
    Objects.requireNonNull(valueType, "valueType must not be null");
    if (!boxed(valueType).isInstance(value)) {
      throw new IllegalArgumentException(
          "value must be an instance of " + valueType.getName() + ": " + value.getClass());
    }
  }

  private static Class<?> boxed(Class<?> type) {
    if (!type.isPrimitive()) {
      return type;
    }
    if (type == int.class) {
      return Integer.class;
    }
    if (type == long.class) {
      return Long.class;
    }
    if (type == boolean.class) {
      return Boolean.class;
    }
    if (type == double.class) {
      return Double.class;
    }
    if (type == float.class) {
      return Float.class;
    }
    if (type == short.class) {
      return Short.class;
    }
    if (type == byte.class) {
      return Byte.class;
    }
    if (type == char.class) {
      return Character.class;
    }
    return type;
  }
}
