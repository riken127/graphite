package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.model.Sort;
import io.github.riken127.graphite.core.model.SortDirection;
import io.github.riken127.graphite.core.model.UnionQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Entry point for creating Graphite DSL objects. */
public final class Graphite {

  private Graphite() {}

  /** Starts a general query composed from ordered typed clauses. */
  public static ClauseQueryBuilder query() {
    return new ClauseQueryBuilder();
  }

  /** Starts a subquery builder with explicit imported variables in scope. */
  public static ClauseQueryBuilder subquery(String... imports) {
    Objects.requireNonNull(imports, "imports must not be null");
    return new ClauseQueryBuilder(List.of(imports));
  }

  /** Joins compatible query branches with UNION. */
  public static UnionQuery union(ClauseQuery first, ClauseQuery... remaining) {
    return buildUnion(false, first, remaining);
  }

  /** Joins compatible query branches with UNION ALL. */
  public static UnionQuery unionAll(ClauseQuery first, ClauseQuery... remaining) {
    return buildUnion(true, first, remaining);
  }

  private static UnionQuery buildUnion(boolean all, ClauseQuery first, ClauseQuery... remaining) {
    Objects.requireNonNull(first, "first must not be null");
    Objects.requireNonNull(remaining, "remaining must not be null");
    List<ClauseQuery> branches = new ArrayList<>();
    branches.add(first);
    branches.addAll(List.of(remaining));
    UnionQuery query = new UnionQuery(branches, all);
    io.github.riken127.graphite.core.validation.QueryValidator.validate(query);
    return query;
  }

  /** Starts a reusable path pattern. */
  public static PathBuilder path(NodePattern start) {
    return new PathBuilder(start);
  }

  /**
   * Starts a MATCH query for the provided node pattern.
   *
   * @param nodePattern node pattern to match
   * @return fluent MATCH query builder
   */
  public static MatchBuilder match(NodePattern nodePattern) {
    return new MatchBuilder(Objects.requireNonNull(nodePattern, "nodePattern must not be null"));
  }

  /**
   * Starts a CREATE query for the provided node pattern.
   *
   * @param nodePattern node pattern to create
   * @return fluent CREATE query builder
   */
  public static CreateBuilder create(NodePattern nodePattern) {
    return new CreateBuilder(Objects.requireNonNull(nodePattern, "nodePattern must not be null"));
  }

  /**
   * Starts a MERGE query for the provided node pattern.
   *
   * @param nodePattern node pattern to merge
   * @return fluent MERGE query builder
   */
  public static MergeBuilder merge(NodePattern nodePattern) {
    return new MergeBuilder(Objects.requireNonNull(nodePattern, "nodePattern must not be null"));
  }

  /**
   * Creates a node pattern from a Java type.
   *
   * @param javaType mapped type
   * @return node pattern with default alias {@code n}
   */
  public static NodePattern node(Class<?> javaType) {
    Objects.requireNonNull(javaType, "javaType must not be null");
    return node(javaType.getSimpleName());
  }

  /**
   * Creates a node pattern from a graph label.
   *
   * @param label graph label
   * @return node pattern with default alias {@code n}
   */
  public static NodePattern node(String label) {
    return new NodePattern(label, "n");
  }

  /**
   * Creates a property reference for predicates.
   *
   * @param alias node alias
   * @param property property name
   * @return property reference
   */
  public static PropertyRef property(String alias, String property) {
    return new PropertyRef(alias, property);
  }

  /** Creates a property reference with compile-time value constraints. */
  public static <T> TypedPropertyRef<T> property(
      String alias, String property, Class<T> valueType) {
    return new TypedPropertyRef<>(alias, property, valueType);
  }

  /**
   * Creates ascending sort criteria.
   *
   * @param alias node alias
   * @param property property name
   * @return ascending sort expression
   */
  public static Sort asc(String alias, String property) {
    return new Sort(alias, property, SortDirection.ASC);
  }

  /**
   * Creates descending sort criteria.
   *
   * @param alias node alias
   * @param property property name
   * @return descending sort expression
   */
  public static Sort desc(String alias, String property) {
    return new Sort(alias, property, SortDirection.DESC);
  }
}
