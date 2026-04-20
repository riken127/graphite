package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable AST model for a MERGE query. */
public record MergeQuery(
    NodePattern nodePattern,
    Map<String, Object> identityProperties,
    Map<String, Object> onCreateProperties,
    Map<String, Object> onMatchProperties,
    List<String> projections)
    implements Query {

  /**
   * Creates a validated MERGE query model.
   *
   * @param nodePattern node pattern to merge
   * @param identityProperties identity properties for merge
   * @param onCreateProperties properties applied in ON CREATE SET
   * @param onMatchProperties properties applied in ON MATCH SET
   * @param projections projection expressions
   */
  public MergeQuery {
    Objects.requireNonNull(nodePattern, "nodePattern must not be null");
    identityProperties = sanitizeProperties(identityProperties, "identityProperties");
    onCreateProperties = sanitizeProperties(onCreateProperties, "onCreateProperties");
    onMatchProperties = sanitizeProperties(onMatchProperties, "onMatchProperties");
    projections = List.copyOf(Objects.requireNonNull(projections, "projections must not be null"));
  }

  private static Map<String, Object> sanitizeProperties(
      Map<String, Object> source, String fieldName) {
    Map<String, Object> sanitizedProperties = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry :
        Objects.requireNonNull(source, fieldName + " must not be null").entrySet()) {
      String propertyName = AstValidator.requireProperty(entry.getKey());
      Object propertyValue =
          Objects.requireNonNull(entry.getValue(), "property value must not be null");
      sanitizedProperties.put(propertyName, propertyValue);
    }
    return Collections.unmodifiableMap(sanitizedProperties);
  }
}
