package io.github.riken127.graphite.metadata;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Typed entity metamodel used by generated property descriptors. */
public final class GraphMetamodel<T> {

  private final Class<T> javaType;
  private final NodeMetadata metadata;
  private final List<GraphAttribute<T, ?>> attributes;

  private GraphMetamodel(Class<T> javaType, String label, List<GraphAttribute<T, ?>> attributes) {
    this.javaType = Objects.requireNonNull(javaType, "javaType must not be null");
    this.attributes = List.copyOf(attributes);
    Set<String> graphNames = new HashSet<>();
    boolean foundId = false;
    for (GraphAttribute<T, ?> attribute : attributes) {
      if (!javaType.equals(attribute.ownerType())) {
        throw new MetadataException(
            "attribute '" + attribute.javaName() + "' has a different owner type");
      }
      if (!graphNames.add(attribute.graphName())) {
        throw new MetadataException("duplicate graph property '" + attribute.graphName() + "'");
      }
      if (attribute.id() && foundId) {
        throw new MetadataException("multiple identity attributes on " + javaType.getName());
      }
      foundId |= attribute.id();
    }
    this.metadata =
        new NodeMetadata(
            javaType,
            AstValidator.requireLabel(label),
            attributes.stream().map(GraphAttribute::metadata).toList());
  }

  /** Creates a typed metamodel from generated attributes. */
  @SafeVarargs
  public static <T> GraphMetamodel<T> of(
      Class<T> javaType, String label, GraphAttribute<T, ?>... attributes) {
    Objects.requireNonNull(attributes, "attributes must not be null");
    return new GraphMetamodel<>(javaType, label, Arrays.asList(attributes));
  }

  /** Creates an entity reference with the default alias {@code n}. */
  public GraphEntity<T> entity() {
    return entity("n");
  }

  /** Creates an entity reference with an explicit alias. */
  public GraphEntity<T> entity(String alias) {
    return new GraphEntity<>(javaType, metadata, alias);
  }

  public Class<T> javaType() {
    return javaType;
  }

  public String label() {
    return metadata.label();
  }

  public List<GraphAttribute<T, ?>> attributes() {
    return attributes;
  }

  public NodeMetadata metadata() {
    return metadata;
  }
}
