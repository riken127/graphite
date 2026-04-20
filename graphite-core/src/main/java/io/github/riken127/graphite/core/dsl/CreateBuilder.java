package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.validation.AstValidator;
import io.github.riken127.graphite.core.validation.QueryValidator;
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
   * Adds multiple properties to the created node.
   *
   * @param propertyValues property map
   * @return current builder
   */
  public CreateBuilder setAll(Map<String, Object> propertyValues) {
    Objects.requireNonNull(propertyValues, "propertyValues must not be null");
    for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
      set(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /**
   * Defines projection expressions for the RETURN clause.
   *
   * @param expressions projections to return
   * @return current builder
   */
  public CreateBuilder select(String... expressions) {
    Objects.requireNonNull(expressions, "expressions must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(expressions));
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
