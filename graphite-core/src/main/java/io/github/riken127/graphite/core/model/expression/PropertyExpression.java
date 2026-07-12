package io.github.riken127.graphite.core.model.expression;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Typed property access on a query variable. */
public record PropertyExpression<T>(String alias, String property, Class<T> valueType)
    implements Expression<T> {

  /** Creates a validated property expression. */
  public PropertyExpression {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
    Objects.requireNonNull(valueType, "valueType must not be null");
  }
}
