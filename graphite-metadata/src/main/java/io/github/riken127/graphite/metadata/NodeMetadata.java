package io.github.riken127.graphite.metadata;

import java.util.Objects;

/** Metadata about a graph node mapping. */
public record NodeMetadata(Class<?> javaType, String label) {

  /**
   * Creates validated node metadata.
   *
   * @param javaType mapped Java type
   * @param label graph node label
   */
  public NodeMetadata {
    Objects.requireNonNull(javaType, "javaType must not be null");
    if (label == null || label.isBlank()) {
      throw new IllegalArgumentException("label must not be blank");
    }
    label = label.trim();
  }
}
