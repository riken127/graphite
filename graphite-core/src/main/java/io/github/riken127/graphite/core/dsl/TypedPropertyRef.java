package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.model.expression.ParameterExpression;
import io.github.riken127.graphite.core.model.expression.PropertyExpression;
import io.github.riken127.graphite.core.model.predicate.ComparisonOperator;
import io.github.riken127.graphite.core.model.predicate.ExpressionComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionInPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionNullPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionTextPredicate;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.model.predicate.TextOperator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Property reference whose accepted values are constrained by its Java value type. */
public final class TypedPropertyRef<T> implements Expression<T> {

  private final PropertyExpression<T> expression;

  public TypedPropertyRef(String alias, String property, Class<T> valueType) {
    this.expression = new PropertyExpression<>(alias, property, valueType);
  }

  public String alias() {
    return expression.alias();
  }

  public String property() {
    return expression.property();
  }

  @Override
  public Class<T> valueType() {
    return expression.valueType();
  }

  /** Compares this property with a typed value. */
  public Predicate eq(T value) {
    return compare(ComparisonOperator.EQ, parameter(value));
  }

  /** Compares this property with another expression of the same value type. */
  public Predicate eq(Expression<T> other) {
    return compare(ComparisonOperator.EQ, other);
  }

  /** Builds a typed non-equality comparison. */
  public Predicate ne(T value) {
    return compare(ComparisonOperator.NE, parameter(value));
  }

  /** Builds a typed greater-than comparison. */
  public Predicate gt(T value) {
    return compare(ComparisonOperator.GT, parameter(value));
  }

  /** Builds a typed greater-than-or-equal comparison. */
  public Predicate gte(T value) {
    return compare(ComparisonOperator.GTE, parameter(value));
  }

  /** Builds a typed less-than comparison. */
  public Predicate lt(T value) {
    return compare(ComparisonOperator.LT, parameter(value));
  }

  /** Builds a typed less-than-or-equal comparison. */
  public Predicate lte(T value) {
    return compare(ComparisonOperator.LTE, parameter(value));
  }

  /** Checks whether this property occurs in the provided typed values. */
  @SuppressWarnings("unchecked")
  public Predicate in(Collection<? extends T> values) {
    Objects.requireNonNull(values, "values must not be null");
    List<T> copied = values.stream().map(this::requireValue).toList();
    if (copied.isEmpty()) {
      throw new IllegalArgumentException("values must not be empty");
    }
    Class<Collection<?>> collectionType = (Class<Collection<?>>) (Class<?>) Collection.class;
    return new ExpressionInPredicate(this, new ParameterExpression<>(copied, collectionType));
  }

  /** Builds a typed STARTS WITH predicate for a string property. */
  public Predicate startsWith(String value) {
    return text(TextOperator.STARTS_WITH, value);
  }

  /** Builds a typed ENDS WITH predicate for a string property. */
  public Predicate endsWith(String value) {
    return text(TextOperator.ENDS_WITH, value);
  }

  /** Builds a typed CONTAINS predicate for a string property. */
  public Predicate contains(String value) {
    return text(TextOperator.CONTAINS, value);
  }

  /** Checks whether this property's value is null. */
  public Predicate isNull() {
    return new ExpressionNullPredicate(this, true);
  }

  /** Checks whether this property's value is non-null. */
  public Predicate isNotNull() {
    return new ExpressionNullPredicate(this, false);
  }

  private Predicate compare(ComparisonOperator operator, Expression<T> right) {
    return new ExpressionComparisonPredicate(this, operator, right);
  }

  private ParameterExpression<T> parameter(T value) {
    return new ParameterExpression<>(requireValue(value), valueType());
  }

  private T requireValue(T value) {
    return Objects.requireNonNull(value, "value must not be null");
  }

  @SuppressWarnings("unchecked")
  private Predicate text(TextOperator operator, String value) {
    if (valueType() != String.class) {
      throw new IllegalStateException("text predicates require a String property");
    }
    Expression<String> stringProperty = (Expression<String>) this;
    return new ExpressionTextPredicate(
        stringProperty, operator, new ParameterExpression<>(value, String.class));
  }
}
