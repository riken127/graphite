package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.model.Sort;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.validation.QueryValidator;
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
  private Integer skip;
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
   * Defines projection expressions for the RETURN clause.
   *
   * @param expressions projections to return
   * @return current builder
   */
  public MatchBuilder select(String... expressions) {
    Objects.requireNonNull(expressions, "expressions must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(expressions));
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
   * Skips the first N rows.
   *
   * @param rows number of rows to skip
   * @return current builder
   */
  public MatchBuilder skip(int rows) {
    if (rows < 0) {
      throw new IllegalArgumentException("skip must be >= 0");
    }
    this.skip = rows;
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

    MatchQuery query = new MatchQuery(nodePattern, predicates, selected, sorts, skip, limit);
    QueryValidator.validate(query);
    return query;
  }
}
