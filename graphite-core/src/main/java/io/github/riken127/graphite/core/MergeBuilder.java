package io.github.riken127.graphite.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Fluent builder for {@link MergeQuery}. */
public final class MergeBuilder {

  private final NodePattern nodePattern;
  private final Map<String, Object> properties;
  private final List<String> projections;

  MergeBuilder(NodePattern nodePattern) {
    this.nodePattern = Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    this.properties = new LinkedHashMap<>();
    this.projections = new ArrayList<>();
  }

  /**
   * Adds a property to the MERGE identity pattern.
   *
   * @param propertyName property name
   * @param value property value
   * @return current builder
   */
  public MergeBuilder on(String propertyName, Object value) {
    properties.put(
        AstValidator.requireProperty(propertyName),
        Objects.requireNonNull(value, "value must not be null"));
    return this;
  }

  /**
   * Defines projection aliases for the RETURN clause.
   *
   * @param aliases aliases to project
   * @return current builder
   */
  public MergeBuilder select(String... aliases) {
    Objects.requireNonNull(aliases, "aliases must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(aliases));
    return this;
  }

  /**
   * Builds an immutable query model.
   *
   * @return validated MERGE query
   */
  public MergeQuery build() {
    List<String> selected =
        projections.isEmpty() ? List.of(nodePattern.alias()) : List.copyOf(projections);
    MergeQuery query = new MergeQuery(nodePattern, properties, selected);
    QueryValidator.validate(query);
    return query;
  }
}
