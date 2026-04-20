package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Sort expression used in ORDER BY clauses. */
public record Sort(String alias, String property, SortDirection direction) {

  /**
   * Creates a validated sort expression.
   *
   * @param alias alias name
   * @param property property name
   * @param direction sort direction
   */
  public Sort {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
    Objects.requireNonNull(direction, "direction must not be null");
  }
}
