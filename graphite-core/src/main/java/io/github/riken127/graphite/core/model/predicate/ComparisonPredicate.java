package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Binary comparison predicate for alias property values. */
public record ComparisonPredicate(
    String alias, String property, ComparisonOperator operator, Object value) implements Predicate {

  /**
   * Creates a validated comparison predicate.
   *
   * @param alias alias name
   * @param property property name
   * @param operator comparison operator
   * @param value comparison value
   */
  public ComparisonPredicate {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
    Objects.requireNonNull(operator, "operator must not be null");
    Objects.requireNonNull(value, "value must not be null");
  }
}
