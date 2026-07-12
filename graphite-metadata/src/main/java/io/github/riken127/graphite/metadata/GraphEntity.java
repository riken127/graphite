package io.github.riken127.graphite.metadata;

import io.github.riken127.graphite.core.dsl.TypedPropertyRef;
import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.model.expression.VariableExpression;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** Metadata-backed, alias-qualified Java entity reference for typed query construction. */
public final class GraphEntity<T> {

  private final Class<T> javaType;
  private final NodeMetadata metadata;
  private final String alias;

  GraphEntity(Class<T> javaType, NodeMetadata metadata, String alias) {
    this.javaType = Objects.requireNonNull(javaType, "javaType must not be null");
    this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
    this.alias = AstValidator.requireAlias(alias);
  }

  /** Returns a copy qualified by a different query alias. */
  public GraphEntity<T> as(String value) {
    return new GraphEntity<>(javaType, metadata, value);
  }

  /** Returns the metadata-resolved node pattern. */
  public NodePattern node() {
    return new NodePattern(metadata.label(), alias);
  }

  /** Returns a typed reference to the complete entity variable. */
  public VariableExpression<T> variable() {
    return new VariableExpression<>(alias, javaType);
  }

  /** Resolves and type-checks a Java property name against graph metadata. */
  public <V> TypedPropertyRef<V> property(String javaName, Class<V> valueType) {
    Objects.requireNonNull(valueType, "valueType must not be null");
    PropertyMetadata property =
        metadata
            .property(javaName)
            .orElseThrow(
                () ->
                    new MetadataException(
                        "unknown property '" + javaName + "' on " + javaType.getName()));
    if (!boxed(valueType).equals(boxed(property.javaType()))) {
      throw new MetadataException(
          "property '"
              + javaName
              + "' on "
              + javaType.getName()
              + " has type "
              + property.javaType().getName()
              + ", not "
              + valueType.getName());
    }
    return new TypedPropertyRef<>(alias, property.graphName(), valueType);
  }

  /** Returns the mapped Java type. */
  public Class<T> javaType() {
    return javaType;
  }

  /** Returns the current query alias. */
  public String alias() {
    return alias;
  }

  private static Class<?> boxed(Class<?> type) {
    if (!type.isPrimitive()) {
      return type;
    }
    if (type == int.class) {
      return Integer.class;
    }
    if (type == long.class) {
      return Long.class;
    }
    if (type == boolean.class) {
      return Boolean.class;
    }
    if (type == double.class) {
      return Double.class;
    }
    if (type == float.class) {
      return Float.class;
    }
    if (type == short.class) {
      return Short.class;
    }
    if (type == byte.class) {
      return Byte.class;
    }
    if (type == char.class) {
      return Character.class;
    }
    return type;
  }
}
