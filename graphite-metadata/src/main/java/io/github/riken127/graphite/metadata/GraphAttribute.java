package io.github.riken127.graphite.metadata;

import io.github.riken127.graphite.core.dsl.TypedPropertyRef;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Generated or programmatic typed description of one mapped graph property. */
public final class GraphAttribute<E, V> {

  private final Class<E> ownerType;
  private final PropertyMetadata metadata;
  private final Class<V> valueType;

  private GraphAttribute(
      Class<E> ownerType,
      String javaName,
      String graphName,
      Class<V> valueType,
      boolean id,
      int constructorIndex) {
    this.ownerType = Objects.requireNonNull(ownerType, "ownerType must not be null");
    this.valueType = Objects.requireNonNull(valueType, "valueType must not be null");
    this.metadata = new PropertyMetadata(javaName, graphName, valueType, id, constructorIndex);
  }

  /** Creates one typed metamodel attribute. */
  public static <E, V> GraphAttribute<E, V> of(
      Class<E> ownerType,
      String javaName,
      String graphName,
      Class<V> valueType,
      boolean id,
      int constructorIndex) {
    return new GraphAttribute<>(ownerType, javaName, graphName, valueType, id, constructorIndex);
  }

  /** Creates an alias-qualified typed property reference. */
  public TypedPropertyRef<V> of(String alias) {
    return new TypedPropertyRef<>(
        AstValidator.requireAlias(alias), metadata.graphName(), valueType);
  }

  /** Creates a typed property reference qualified by a matching entity. */
  public TypedPropertyRef<V> of(GraphEntity<E> entity) {
    Objects.requireNonNull(entity, "entity must not be null");
    if (!ownerType.equals(entity.javaType())) {
      throw new MetadataException(
          "attribute owner "
              + ownerType.getName()
              + " does not match entity "
              + entity.javaType().getName());
    }
    return of(entity.alias());
  }

  public Class<E> ownerType() {
    return ownerType;
  }

  public String javaName() {
    return metadata.javaName();
  }

  public String graphName() {
    return metadata.graphName();
  }

  public Class<V> valueType() {
    return valueType;
  }

  public boolean id() {
    return metadata.id();
  }

  PropertyMetadata metadata() {
    return metadata;
  }
}
