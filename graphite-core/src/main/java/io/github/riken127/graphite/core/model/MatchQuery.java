package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.List;
import java.util.Objects;

/** Immutable AST model for a MATCH query. */
public record MatchQuery(
    NodePattern nodePattern,
    List<Predicate> predicates,
    List<String> projections,
    List<Sort> sorts,
    Integer skip,
    Integer limit)
    implements Query {

  /**
   * Creates a validated MATCH query model.
   *
   * @param nodePattern node pattern
   * @param predicates predicate list
   * @param projections projection expressions
   * @param sorts sort expressions
   * @param skip skipped rows or {@code null}
   * @param limit result limit or {@code null}
   */
  public MatchQuery {
    Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    predicates = List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));
    sorts = List.copyOf(Objects.requireNonNull(sorts, "sorts must not be null"));

    if (skip != null && skip < 0) {
      throw new IllegalArgumentException("skip must be >= 0");
    }
    if (limit != null && limit <= 0) {
      throw new IllegalArgumentException("limit must be > 0");
    }
  }
}
