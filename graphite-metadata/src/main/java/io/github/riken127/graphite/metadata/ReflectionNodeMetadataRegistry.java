package io.github.riken127.graphite.metadata;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Thread-safe reflection metadata registry for Java records. */
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
    if (!javaType.isRecord()) {
      throw new MetadataException("mapped type must be a Java record: " + javaType.getName());
    }

    GraphNode graphNode = javaType.getAnnotation(GraphNode.class);
    String label =
        AstValidator.requireLabel(graphNode == null ? javaType.getSimpleName() : graphNode.value());
    RecordComponent[] components = javaType.getRecordComponents();
    List<PropertyMetadata> properties = new ArrayList<>(components.length);
    Set<String> graphNames = new HashSet<>();
    boolean foundId = false;

    for (int index = 0; index < components.length; index++) {
      RecordComponent component = components[index];
      GraphProperty graphProperty = component.getAnnotation(GraphProperty.class);
      String graphName =
          AstValidator.requireProperty(
              graphProperty == null ? component.getName() : graphProperty.value());
      if (!graphNames.add(graphName)) {
        throw new MetadataException(
            "duplicate graph property '" + graphName + "' on " + javaType.getName());
      }
      boolean id = component.isAnnotationPresent(GraphId.class);
      if (id && foundId) {
        throw new MetadataException("multiple @GraphId components on " + javaType.getName());
      }
      foundId |= id;
      properties.add(
          new PropertyMetadata(component.getName(), graphName, component.getType(), id, index));
    }

    return new NodeMetadata(javaType, label, properties);
  }
}
