package io.github.riken127.graphite.core.model.expression;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Typed function call expression, including aggregate functions. */
public record FunctionExpression<T>(
    String name, List<Expression<?>> arguments, Class<T> valueType, boolean distinct)
    implements Expression<T> {

  private static final Pattern FUNCTION_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");

  /** Creates a validated function expression. */
  public FunctionExpression {
    if (name == null || !FUNCTION_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException("invalid function name: " + name);
    }
    arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
    arguments.forEach(argument -> Objects.requireNonNull(argument, "argument must not be null"));
    Objects.requireNonNull(valueType, "valueType must not be null");
    if (distinct && arguments.size() != 1) {
      throw new IllegalArgumentException("DISTINCT functions require exactly one argument");
    }
  }
}
