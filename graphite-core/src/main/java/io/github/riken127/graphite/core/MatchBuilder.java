package io.github.riken127.graphite.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Fluent builder for {@link MatchQuery}. */
public final class MatchBuilder {

  private final NodePattern nodePattern;
  private final List<Predicate> predicates;
  private final List<String> projections;
  private final List<Sort> sorts;
  private Integer limit;

  MatchBuilder(NodePattern nodePattern) {
    this.nodePattern = Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    this.predicates = new ArrayList<>();
    this.projections = new ArrayList<>();
    this.sorts = new ArrayList<>();
  }

  /**
   * Adds a predicate to the query.
   *
   * @param predicate predicate to append
   * @return current builder
   */
  public MatchBuilder where(Predicate predicate) {
    predicates.add(Objects.requireNonNull(predicate, "predicate must not be null"));
    return this;
  }

  /**
   * Defines projection aliases for the RETURN clause.
   *
   * @param aliases aliases to project
   * @return current builder
   */
  public MatchBuilder select(String... aliases) {
    Objects.requireNonNull(aliases, "aliases must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(aliases));
    return this;
  }

  /**
   * Adds a sort expression.
   *
   * @param sort sort expression
   * @return current builder
   */
  public MatchBuilder orderBy(Sort sort) {
    sorts.add(Objects.requireNonNull(sort, "sort must not be null"));
    return this;
  }

  /**
   * Applies a result limit.
   *
   * @param maxRows max rows to return
   * @return current builder
   */
  public MatchBuilder limit(int maxRows) {
    if (maxRows <= 0) {
      throw new IllegalArgumentException("limit must be > 0");
    }
    this.limit = maxRows;
    return this;
  }

  /**
   * Builds an immutable query model.
   *
   * @return validated MATCH query
   */
  public MatchQuery build() {
    List<String> selected =
        projections.isEmpty() ? List.of(nodePattern.alias()) : List.copyOf(projections);
    MatchQuery query = new MatchQuery(nodePattern, predicates, selected, sorts, limit);
    QueryValidator.validate(query);
    return query;
  }
}
