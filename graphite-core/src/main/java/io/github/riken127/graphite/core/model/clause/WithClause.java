package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.Projection;
import java.util.List;
import java.util.Objects;

/** WITH projection and scope boundary. */
public record WithClause(List<Projection> projections, boolean distinct) implements Clause {

  /** Creates a validated WITH clause. */
  public WithClause {
    projections = copyRequired(projections);
  }

  private static List<Projection> copyRequired(List<Projection> values) {
    List<Projection> copied =
        List.copyOf(Objects.requireNonNull(values, "projections must not be null"));
    if (copied.isEmpty()) {
      throw new IllegalArgumentException("projections must not be empty");
    }
    copied.forEach(value -> Objects.requireNonNull(value, "projection must not be null"));
    return copied;
  }
}
