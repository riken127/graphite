package io.github.riken127.graphite.core.model.clause;

import java.util.List;
import java.util.Objects;

/** SET clause containing one or more typed property assignments. */
public record SetClause(List<SetAssignment<?>> assignments) implements Clause {

  /** Creates a validated SET clause. */
  public SetClause {
    assignments = List.copyOf(Objects.requireNonNull(assignments, "assignments must not be null"));
    if (assignments.isEmpty()) {
      throw new IllegalArgumentException("assignments must not be empty");
    }
    assignments.forEach(value -> Objects.requireNonNull(value, "assignment must not be null"));
  }
}
