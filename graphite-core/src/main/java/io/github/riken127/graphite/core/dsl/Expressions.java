package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.SortDirection;
import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.model.expression.ExpressionSort;
import io.github.riken127.graphite.core.model.expression.FunctionExpression;
import io.github.riken127.graphite.core.model.expression.ParameterExpression;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.core.model.expression.VariableExpression;
import java.util.List;
import java.util.Objects;

/** Factory methods for typed query expressions and projections. */
public final class Expressions {

  private Expressions() {}

  /** Creates a typed query variable reference. */
  public static <T> VariableExpression<T> variable(String alias, Class<T> valueType) {
    return new VariableExpression<>(alias, valueType);
  }

  /** Creates a parameterized literal expression. */
  public static <T> ParameterExpression<T> value(T value, Class<T> valueType) {
    return new ParameterExpression<>(value, valueType);
  }

  /** Creates a typed function call. */
  public static <T> FunctionExpression<T> function(
      String name, Class<T> valueType, Expression<?>... arguments) {
    Objects.requireNonNull(arguments, "arguments must not be null");
    return new FunctionExpression<>(name, List.of(arguments), valueType, false);
  }

  /** Creates a COUNT aggregate. */
  public static FunctionExpression<Long> count(Expression<?> expression) {
    return new FunctionExpression<>("count", List.of(expression), Long.class, false);
  }

  /** Creates a COUNT DISTINCT aggregate. */
  public static FunctionExpression<Long> countDistinct(Expression<?> expression) {
    return new FunctionExpression<>("count", List.of(expression), Long.class, true);
  }

  /** Creates a COLLECT aggregate. */
  @SuppressWarnings("unchecked")
  public static <T> FunctionExpression<List<T>> collect(Expression<T> expression) {
    return new FunctionExpression<>(
        "collect", List.of(expression), (Class<List<T>>) (Class<?>) List.class, false);
  }

  /** Creates an unaliased projection. */
  public static Projection project(Expression<?> expression) {
    return Projection.of(expression);
  }

  /** Creates an aliased projection. */
  public static Projection project(Expression<?> expression, String alias) {
    return Projection.as(expression, alias);
  }

  /** Creates ascending expression sort criteria. */
  public static ExpressionSort asc(Expression<?> expression) {
    return new ExpressionSort(expression, SortDirection.ASC);
  }

  /** Creates descending expression sort criteria. */
  public static ExpressionSort desc(Expression<?> expression) {
    return new ExpressionSort(expression, SortDirection.DESC);
  }
}
