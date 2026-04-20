package io.github.riken127.graphite.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Fluent builder for {@link CreateQuery}. */
public final class CreateBuilder {

  private final NodePattern nodePattern;
  private final Map<String, Object> properties;
  private final List<String> projections;

  CreateBuilder(NodePattern nodePattern) {
    this.nodePattern = Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    this.properties = new LinkedHashMap<>();
    this.projections = new ArrayList<>();
  }

  /**
   * Adds a property to the created node.
   *
   * @param propertyName property name
   * @param value property value
   * @return current builder
   */
  public CreateBuilder set(String propertyName, Object value) {
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
  public CreateBuilder select(String... aliases) {
    Objects.requireNonNull(aliases, "aliases must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(aliases));
    return this;
  }

  /**
   * Builds an immutable query model.
   *
   * @return validated CREATE query
   */
  public CreateQuery build() {
    List<String> selected =
        projections.isEmpty() ? List.of(nodePattern.alias()) : List.copyOf(projections);
    CreateQuery query = new CreateQuery(nodePattern, properties, selected);
    QueryValidator.validate(query);
    return query;
  }
}
