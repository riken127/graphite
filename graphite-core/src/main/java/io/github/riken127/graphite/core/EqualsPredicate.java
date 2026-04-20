package io.github.riken127.graphite.core;

import java.util.Objects;

/** Equality predicate for alias property values. */
public record EqualsPredicate(String alias, String property, Object value) implements Predicate {

  /**
   * Creates a validated equality predicate.
   *
   * @param alias alias name
   * @param property property name
   * @param value value to compare against
   */
  public EqualsPredicate {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
    Objects.requireNonNull(value, "value must not be null");
  }
}
