package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.Expression;
import java.util.List;
import java.util.Objects;

/** REMOVE clause for property expressions. */
public record RemoveClause(List<Expression<?>> properties) implements Clause {

  /** Creates a validated REMOVE clause. */
  public RemoveClause {
    properties = List.copyOf(Objects.requireNonNull(properties, "properties must not be null"));
    if (properties.isEmpty()) {
      throw new IllegalArgumentException("properties must not be empty");
    }
    properties.forEach(value -> Objects.requireNonNull(value, "property must not be null"));
  }
}
