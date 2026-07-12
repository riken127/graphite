package io.github.riken127.graphite.metadata;

/** Resolves and caches Java record metadata. */
public interface NodeMetadataRegistry {

  NodeMetadata metadata(Class<?> javaType);
}
