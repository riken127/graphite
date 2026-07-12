package io.github.riken127.graphite.metadata;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Immutable mapping between a Java record component and a graph property. */
public record PropertyMetadata(
    String javaName, String graphName, Class<?> javaType, boolean id, int constructorIndex) {

  /** Validates and creates property metadata. */
  public PropertyMetadata {
    javaName = AstValidator.requireProperty(javaName);
    graphName = AstValidator.requireProperty(graphName);
    Objects.requireNonNull(javaType, "javaType must not be null");
    if (constructorIndex < 0) {
      throw new IllegalArgumentException("constructorIndex must be non-negative");
    }
  }
}
