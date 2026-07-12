package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.PathPattern;
import java.util.List;
import java.util.Objects;

/** MERGE pattern with optional ON CREATE and ON MATCH assignments. */
public record MergeClause(
    PathPattern pattern,
    List<SetAssignment<?>> onCreateAssignments,
    List<SetAssignment<?>> onMatchAssignments)
    implements Clause {

  /** Creates a validated MERGE clause. */
  public MergeClause {
    Objects.requireNonNull(pattern, "pattern must not be null");
    onCreateAssignments = copy(onCreateAssignments, "onCreateAssignments");
    onMatchAssignments = copy(onMatchAssignments, "onMatchAssignments");
  }

  private static List<SetAssignment<?>> copy(List<SetAssignment<?>> values, String field) {
    List<SetAssignment<?>> copied =
        List.copyOf(Objects.requireNonNull(values, field + " must not be null"));
    copied.forEach(value -> Objects.requireNonNull(value, "assignment must not be null"));
    return copied;
  }
}
