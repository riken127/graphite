package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.validation.AstValidator;

/** NULL predicate for alias properties. */
public record NullPredicate(String alias, String property, boolean isNull) implements Predicate {

  /**
   * Creates a validated NULL predicate.
   *
   * @param alias alias name
   * @param property property name
   * @param isNull true for IS NULL, false for IS NOT NULL
   */
  public NullPredicate {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
  }
}
