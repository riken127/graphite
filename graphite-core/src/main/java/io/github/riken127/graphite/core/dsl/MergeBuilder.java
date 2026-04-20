package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.validation.AstValidator;
import io.github.riken127.graphite.core.validation.QueryValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Fluent builder for {@link MergeQuery}. */
public final class MergeBuilder {

  private final NodePattern nodePattern;
  private final Map<String, Object> identityProperties;
  private final Map<String, Object> onCreateProperties;
  private final Map<String, Object> onMatchProperties;
  private final List<String> projections;

  MergeBuilder(NodePattern nodePattern) {
    this.nodePattern = Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    this.identityProperties = new LinkedHashMap<>();
    this.onCreateProperties = new LinkedHashMap<>();
    this.onMatchProperties = new LinkedHashMap<>();
    this.projections = new ArrayList<>();
  }

  /**
   * Adds an identity property to the MERGE pattern.
   *
   * @param propertyName property name
   * @param value property value
   * @return current builder
   */
  public MergeBuilder on(String propertyName, Object value) {
    identityProperties.put(
        AstValidator.requireProperty(propertyName),
        Objects.requireNonNull(value, "value must not be null"));
    return this;
  }

  /**
   * Adds multiple identity properties to the MERGE pattern.
   *
   * @param propertyValues property map
   * @return current builder
   */
  public MergeBuilder onAll(Map<String, Object> propertyValues) {
    Objects.requireNonNull(propertyValues, "propertyValues must not be null");
    for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
      on(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /**
   * Adds a property in the ON CREATE SET clause.
   *
   * @param propertyName property name
   * @param value property value
   * @return current builder
   */
  public MergeBuilder onCreateSet(String propertyName, Object value) {
    onCreateProperties.put(
        AstValidator.requireProperty(propertyName),
        Objects.requireNonNull(value, "value must not be null"));
    return this;
  }

  /**
   * Adds multiple properties to ON CREATE SET.
   *
   * @param propertyValues property map
   * @return current builder
   */
  public MergeBuilder onCreateSetAll(Map<String, Object> propertyValues) {
    Objects.requireNonNull(propertyValues, "propertyValues must not be null");
    for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
      onCreateSet(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /**
   * Adds a property in the ON MATCH SET clause.
   *
   * @param propertyName property name
   * @param value property value
   * @return current builder
   */
  public MergeBuilder onMatchSet(String propertyName, Object value) {
    onMatchProperties.put(
        AstValidator.requireProperty(propertyName),
        Objects.requireNonNull(value, "value must not be null"));
    return this;
  }

  /**
   * Adds multiple properties to ON MATCH SET.
   *
   * @param propertyValues property map
   * @return current builder
   */
  public MergeBuilder onMatchSetAll(Map<String, Object> propertyValues) {
    Objects.requireNonNull(propertyValues, "propertyValues must not be null");
    for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
      onMatchSet(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /**
   * Defines projection expressions for the RETURN clause.
   *
   * @param expressions projections to return
   * @return current builder
   */
  public MergeBuilder select(String... expressions) {
    Objects.requireNonNull(expressions, "expressions must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(expressions));
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

    MergeQuery query =
        new MergeQuery(
            nodePattern, identityProperties, onCreateProperties, onMatchProperties, selected);
    QueryValidator.validate(query);
    return query;
  }
}
