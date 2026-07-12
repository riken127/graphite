package io.github.riken127.graphite.core.model.expression;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Reference to a node, relationship, or value variable in query scope. */
public record VariableExpression<T>(String alias, Class<T> valueType) implements Expression<T> {

  /** Creates a validated variable expression. */
  public VariableExpression {
    alias = AstValidator.requireAlias(alias);
    Objects.requireNonNull(valueType, "valueType must not be null");
  }
}
