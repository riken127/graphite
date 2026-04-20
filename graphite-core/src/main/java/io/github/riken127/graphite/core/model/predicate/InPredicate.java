package io.github.riken127.graphite.core.model.predicate;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.List;
import java.util.Objects;

/** Membership predicate for alias property values. */
public record InPredicate(String alias, String property, List<Object> values) implements Predicate {

  /**
   * Creates a validated IN predicate.
   *
   * @param alias alias name
   * @param property property name
   * @param values values for membership checks
   */
  public InPredicate {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
    values = List.copyOf(Objects.requireNonNull(values, "values must not be null"));

    if (values.isEmpty()) {
      throw new IllegalArgumentException("values must not be empty");
    }
    for (Object value : values) {
      Objects.requireNonNull(value, "values must not contain null");
    }
  }
}
