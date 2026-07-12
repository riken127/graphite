package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable AST model for a MATCH-based property update. */
public record UpdateQuery(
    PathPattern pathPattern,
    List<Predicate> predicates,
    Map<PropertyTarget, Object> assignments,
    List<PropertyTarget> removals,
    List<String> projections)
    implements Query {

  /** Creates a validated update query model. */
  public UpdateQuery {
    Objects.requireNonNull(pathPattern, "pathPattern must not be null");
    predicates = List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
    removals = List.copyOf(Objects.requireNonNull(removals, "removals must not be null"));
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));

    Map<PropertyTarget, Object> copiedAssignments = new LinkedHashMap<>();
    for (Map.Entry<PropertyTarget, Object> entry :
        Objects.requireNonNull(assignments, "assignments must not be null").entrySet()) {
      copiedAssignments.put(
          Objects.requireNonNull(entry.getKey(), "assignment target must not be null"),
          Objects.requireNonNull(entry.getValue(), "assignment value must not be null"));
    }
    assignments = Collections.unmodifiableMap(copiedAssignments);
  }
}
