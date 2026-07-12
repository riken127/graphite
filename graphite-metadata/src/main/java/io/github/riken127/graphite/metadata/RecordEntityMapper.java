package io.github.riken127.graphite.metadata;

import java.util.Map;
import java.util.Objects;

/** Compatibility facade for applications that map immutable Java records. */
public final class RecordEntityMapper {

  private final GraphObjectMapper delegate;

  public RecordEntityMapper(NodeMetadataRegistry metadataRegistry) {
    this.delegate = new GraphObjectMapper(metadataRegistry);
  }

  /** Creates a record mapper with application-specific value converters. */
  public RecordEntityMapper(
      NodeMetadataRegistry metadataRegistry, GraphValueConverters converters) {
    this.delegate = new GraphObjectMapper(metadataRegistry, converters);
  }

  /** Maps graph properties to an instance of the requested record type. */
  public <T> T map(Class<T> targetType, Map<String, ?> properties) {
    Objects.requireNonNull(targetType, "targetType must not be null");
    if (!targetType.isRecord()) {
      throw new MetadataMappingException(
          "RecordEntityMapper requires a Java record: " + targetType.getName());
    }
    return delegate.map(targetType, properties);
  }
}
