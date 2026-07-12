package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.SortDirection;
import io.github.riken127.graphite.core.model.clause.SetAssignment;
import io.github.riken127.graphite.core.model.expression.CaseAlternative;
import io.github.riken127.graphite.core.model.expression.CaseExpression;
import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.model.expression.ExpressionSort;
import io.github.riken127.graphite.core.model.expression.FunctionExpression;
import io.github.riken127.graphite.core.model.expression.ListExpression;
import io.github.riken127.graphite.core.model.expression.MapExpression;
import io.github.riken127.graphite.core.model.expression.ParameterExpression;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.core.model.expression.VariableExpression;
import java.util.List;
import java.util.Map;
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

  /** Creates a typed list expression. */
  @SafeVarargs
  public static <T> ListExpression<T> list(
      Class<T> elementType, Expression<? extends T>... elements) {
    Objects.requireNonNull(elements, "elements must not be null");
    return new ListExpression<>(List.of(elements), elementType);
  }

  /** Creates a map expression. */
  public static MapExpression map(Map<String, Expression<?>> entries) {
    return new MapExpression(entries);
  }

  /** Creates one branch of a searched CASE expression. */
  public static <T> CaseAlternative<T> when(
      io.github.riken127.graphite.core.model.predicate.Predicate predicate,
      Expression<? extends T> result) {
    return new CaseAlternative<>(predicate, result);
  }

  /** Creates a typed searched CASE expression. */
  @SafeVarargs
  public static <T> CaseExpression<T> caseWhen(
      Class<T> valueType, Expression<? extends T> otherwise, CaseAlternative<T>... alternatives) {
    Objects.requireNonNull(alternatives, "alternatives must not be null");
    return new CaseExpression<>(List.of(alternatives), otherwise, valueType);
  }

  /** Creates a type-safe property assignment. */
  public static <T> SetAssignment<T> set(Expression<T> target, Expression<? extends T> value) {
    return new SetAssignment<>(target, value);
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
