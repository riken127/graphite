package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.List;
import java.util.Objects;

/** Immutable AST model for a MATCH query. */
public record MatchQuery(
    PathPattern pathPattern,
    List<Predicate> predicates,
    List<String> projections,
    List<Sort> sorts,
    Integer skip,
    Integer limit)
    implements Query {

  /**
   * Creates a validated MATCH query model.
   *
   * @param pathPattern path pattern
   * @param predicates predicate list
   * @param projections projection expressions
   * @param sorts sort expressions
   * @param skip skipped rows or {@code null}
   * @param limit result limit or {@code null}
   */
  public MatchQuery {
    Objects.requireNonNull(pathPattern, "pathPattern must not be null");
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

  /** Source-compatible constructor for a single-node MATCH query. */
  public MatchQuery(
      NodePattern nodePattern,
      List<Predicate> predicates,
      List<String> projections,
      List<Sort> sorts,
      Integer skip,
      Integer limit) {
    this(new PathPattern(nodePattern, List.of()), predicates, projections, sorts, skip, limit);
  }

  /** Returns the first node in the matched path. */
  public NodePattern nodePattern() {
    return pathPattern.start();
  }
}
