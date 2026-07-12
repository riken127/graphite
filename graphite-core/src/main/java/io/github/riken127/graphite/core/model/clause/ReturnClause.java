package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.Projection;
import java.util.List;
import java.util.Objects;

/** RETURN clause with typed projections. */
public record ReturnClause(List<Projection> projections, boolean distinct) implements Clause {

  /** Creates a validated RETURN clause. */
  public ReturnClause {
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));
    if (projections.isEmpty()) {
      throw new IllegalArgumentException("projections must not be empty");
    }
    projections.forEach(value -> Objects.requireNonNull(value, "projection must not be null"));
  }
}
