package io.github.riken127.graphite.metadata;

import java.util.Objects;

/** Creates metadata-backed typed entity references for query construction. */
public final class GraphEntityFactory {

  private final NodeMetadataRegistry metadataRegistry;

  public GraphEntityFactory(NodeMetadataRegistry metadataRegistry) {
    this.metadataRegistry =
        Objects.requireNonNull(metadataRegistry, "metadataRegistry must not be null");
  }

  /** Creates an entity reference using the default alias {@code n}. */
  public <T> GraphEntity<T> entity(Class<T> javaType) {
    return entity(javaType, "n");
  }

  /** Creates an entity reference with an explicit query alias. */
  public <T> GraphEntity<T> entity(Class<T> javaType, String alias) {
    Objects.requireNonNull(javaType, "javaType must not be null");
    return new GraphEntity<>(javaType, metadataRegistry.metadata(javaType), alias);
  }
}
