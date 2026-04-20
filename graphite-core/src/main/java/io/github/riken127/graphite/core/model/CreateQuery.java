package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable AST model for a CREATE query. */
public record CreateQuery(
    NodePattern nodePattern, Map<String, Object> properties, List<String> projections)
    implements Query {

  /**
   * Creates a validated CREATE query model.
   *
   * @param nodePattern node pattern to create
   * @param properties node properties
   * @param projections projection expressions
   */
  public CreateQuery {
    Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    properties = sanitizeProperties(properties);
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));
  }

  private static Map<String, Object> sanitizeProperties(Map<String, Object> source) {
    Map<String, Object> sanitizedProperties = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry :
        Objects.requireNonNull(source, "properties must not be null").entrySet()) {
      String propertyName = AstValidator.requireProperty(entry.getKey());
      Object propertyValue =
          Objects.requireNonNull(entry.getValue(), "property value must not be null");
      sanitizedProperties.put(propertyName, propertyValue);
    }
    return Collections.unmodifiableMap(sanitizedProperties);
  }
}
