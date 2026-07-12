package io.github.riken127.graphite.metadata;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe reflection metadata registry for records and constructor-backed immutable classes.
 */
public final class ReflectionNodeMetadataRegistry implements NodeMetadataRegistry {

  private final ConcurrentMap<Class<?>, NodeMetadata> metadataCache = new ConcurrentHashMap<>();

  @Override
  public NodeMetadata metadata(Class<?> javaType) {
    if (javaType == null) {
      throw new IllegalArgumentException("javaType must not be null");
    }
    return metadataCache.computeIfAbsent(javaType, this::inspect);
  }

  /** Returns the current number of cached Java types. */
  public int cachedTypeCount() {
    return metadataCache.size();
  }

  private NodeMetadata inspect(Class<?> javaType) {
    GraphNode graphNode = javaType.getAnnotation(GraphNode.class);
    String label =
        AstValidator.requireLabel(graphNode == null ? javaType.getSimpleName() : graphNode.value());
    List<ReflectionMappingSupport.ReflectedProperty> reflectedProperties =
        ReflectionMappingSupport.properties(
            javaType, ReflectionMappingSupport.constructor(javaType));
    List<PropertyMetadata> properties = new ArrayList<>(reflectedProperties.size());
    Set<String> graphNames = new HashSet<>();
    boolean foundId = false;

    for (ReflectionMappingSupport.ReflectedProperty reflected : reflectedProperties) {
      String graphName = AstValidator.requireProperty(reflected.graphName());
      if (!graphNames.add(graphName)) {
        throw new MetadataException(
            "duplicate graph property '" + graphName + "' on " + javaType.getName());
      }
      boolean id = reflected.id();
      if (id && foundId) {
        throw new MetadataException("multiple @GraphId components on " + javaType.getName());
      }
      foundId |= id;
      properties.add(
          new PropertyMetadata(
              reflected.javaName(),
              graphName,
              reflected.javaType(),
              reflected.genericType(),
              id,
              reflected.constructorIndex()));
    }

    return new NodeMetadata(javaType, label, properties);
  }
}
