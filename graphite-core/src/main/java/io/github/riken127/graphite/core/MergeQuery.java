package io.github.riken127.graphite.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable AST model for a MERGE query. */
public record MergeQuery(
    NodePattern nodePattern, Map<String, Object> properties, List<String> projections)
    implements Query {

  /**
   * Creates a validated MERGE query model.
   *
   * @param nodePattern node pattern to merge
   * @param properties identity properties for merge
   * @param projections projection aliases
   */
  public MergeQuery {
    Objects.requireNonNull(nodePattern, "nodePattern must not be null");

    Map<String, Object> sanitizedProperties = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry :
        Objects.requireNonNull(properties, "properties must not be null").entrySet()) {
      String propertyName = AstValidator.requireProperty(entry.getKey());
      Object propertyValue =
          Objects.requireNonNull(entry.getValue(), "property value must not be null");
      sanitizedProperties.put(propertyName, propertyValue);
    }

    properties = Collections.unmodifiableMap(sanitizedProperties);
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));
  }
}
