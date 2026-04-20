package io.github.riken127.graphite.core;

import java.util.List;
import java.util.Objects;

/** Immutable AST model for a basic MATCH query. */
public record MatchQuery(
    NodePattern nodePattern,
    List<Predicate> predicates,
    List<String> projections,
    List<Sort> sorts,
    Integer limit)
    implements Query {

  /**
   * Creates a validated MATCH query model.
   *
   * @param nodePattern node pattern
   * @param predicates predicate list
   * @param projections projection aliases
   * @param sorts sort expressions
   * @param limit result limit or {@code null}
   */
  public MatchQuery {
    Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    predicates = List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));
    sorts = List.copyOf(Objects.requireNonNull(sorts, "sorts must not be null"));
    if (limit != null && limit <= 0) {
      throw new IllegalArgumentException("limit must be > 0");
    }
  }
}
