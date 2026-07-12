package io.github.riken127.graphite.metadata;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Metadata about a graph node mapping. */
public record NodeMetadata(Class<?> javaType, String label, List<PropertyMetadata> properties) {

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
    properties = List.copyOf(Objects.requireNonNull(properties, "properties must not be null"));
  }

  /** Creates label-only metadata for compatibility with explicit mappings. */
  public NodeMetadata(Class<?> javaType, String label) {
    this(javaType, label, List.of());
  }

  /** Returns the optional application identity property. */
  public Optional<PropertyMetadata> idProperty() {
    return properties.stream().filter(PropertyMetadata::id).findFirst();
  }

  /** Finds metadata by its Java constructor-property name. */
  public Optional<PropertyMetadata> property(String javaName) {
    Objects.requireNonNull(javaName, "javaName must not be null");
    return properties.stream().filter(property -> property.javaName().equals(javaName)).findFirst();
  }
}
