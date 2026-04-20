package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.predicate.ComparisonOperator;
import io.github.riken127.graphite.core.model.predicate.ComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.InPredicate;
import io.github.riken127.graphite.core.model.predicate.NullPredicate;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.model.predicate.TextOperator;
import io.github.riken127.graphite.core.model.predicate.TextPredicate;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Typed reference to a node property used in predicates. */
public final class PropertyRef {

  private final String alias;
  private final String name;

  PropertyRef(String alias, String name) {
    this.alias = AstValidator.requireAlias(alias);
    this.name = AstValidator.requireProperty(name);
  }

  /**
   * Builds an equality predicate.
   *
   * @param value expected value
   * @return equality predicate
   */
  public Predicate eq(Object value) {
    return new ComparisonPredicate(alias, name, ComparisonOperator.EQ, value);
  }

  /**
   * Builds a non-equality predicate.
   *
   * @param value expected value
   * @return non-equality predicate
   */
  public Predicate ne(Object value) {
    return new ComparisonPredicate(alias, name, ComparisonOperator.NE, value);
  }

  /**
   * Builds a greater-than predicate.
   *
   * @param value comparison value
   * @return greater-than predicate
   */
  public Predicate gt(Object value) {
    return new ComparisonPredicate(alias, name, ComparisonOperator.GT, value);
  }

  /**
   * Builds a greater-than-or-equal predicate.
   *
   * @param value comparison value
   * @return greater-than-or-equal predicate
   */
  public Predicate gte(Object value) {
    return new ComparisonPredicate(alias, name, ComparisonOperator.GTE, value);
  }

  /**
   * Builds a less-than predicate.
   *
   * @param value comparison value
   * @return less-than predicate
   */
  public Predicate lt(Object value) {
    return new ComparisonPredicate(alias, name, ComparisonOperator.LT, value);
  }

  /**
   * Builds a less-than-or-equal predicate.
   *
   * @param value comparison value
   * @return less-than-or-equal predicate
   */
  public Predicate lte(Object value) {
    return new ComparisonPredicate(alias, name, ComparisonOperator.LTE, value);
  }

  /**
   * Builds an IN predicate.
   *
   * @param values values for membership checks
   * @return IN predicate
   */
  public Predicate in(Collection<?> values) {
    Objects.requireNonNull(values, "values must not be null");
    List<Object> copiedValues = values.stream().map(PropertyRef::requireValue).toList();
    return new InPredicate(alias, name, copiedValues);
  }

  /**
   * Builds a STARTS WITH predicate.
   *
   * @param value prefix text
   * @return STARTS WITH predicate
   */
  public Predicate startsWith(String value) {
    return new TextPredicate(alias, name, TextOperator.STARTS_WITH, value);
  }

  /**
   * Builds an ENDS WITH predicate.
   *
   * @param value suffix text
   * @return ENDS WITH predicate
   */
  public Predicate endsWith(String value) {
    return new TextPredicate(alias, name, TextOperator.ENDS_WITH, value);
  }

  /**
   * Builds a CONTAINS predicate.
   *
   * @param value contained text
   * @return CONTAINS predicate
   */
  public Predicate contains(String value) {
    return new TextPredicate(alias, name, TextOperator.CONTAINS, value);
  }

  /**
   * Builds an IS NULL predicate.
   *
   * @return IS NULL predicate
   */
  public Predicate isNull() {
    return new NullPredicate(alias, name, true);
  }

  /**
   * Builds an IS NOT NULL predicate.
   *
   * @return IS NOT NULL predicate
   */
  public Predicate isNotNull() {
    return new NullPredicate(alias, name, false);
  }

  private static Object requireValue(Object value) {
    return Objects.requireNonNull(value, "values must not contain null");
  }
}
