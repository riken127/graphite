package io.github.riken127.graphite.core;

import java.util.Objects;

/** Entry point for creating Graphite DSL objects. */
public final class Graphite {

  private Graphite() {}

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
