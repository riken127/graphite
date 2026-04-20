package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Text comparison predicate for alias property values. */
public record TextPredicate(String alias, String property, TextOperator operator, String value)
    implements Predicate {

  /**
   * Creates a validated text predicate.
   *
   * @param alias alias name
   * @param property property name
   * @param operator text operator
   * @param value text value
   */
  public TextPredicate {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
    Objects.requireNonNull(operator, "operator must not be null");
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
    value = value.trim();
  }
}
