package io.github.riken127.graphite.metadata;

/** Resolves and caches mapped Java type metadata. */
public interface NodeMetadataRegistry {

  NodeMetadata metadata(Class<?> javaType);
}
