package io.github.riken127.graphite.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Constructs immutable Java records from graph property maps. */
public final class RecordEntityMapper {

  private final NodeMetadataRegistry metadataRegistry;
  private final ConcurrentMap<Class<?>, Constructor<?>> constructorCache =
      new ConcurrentHashMap<>();

  public RecordEntityMapper(NodeMetadataRegistry metadataRegistry) {
    this.metadataRegistry =
        Objects.requireNonNull(metadataRegistry, "metadataRegistry must not be null");
  }

  /** Maps graph properties to an instance of the requested Java record type. */
  public <T> T map(Class<T> targetType, Map<String, ?> properties) {
    Objects.requireNonNull(targetType, "targetType must not be null");
    Objects.requireNonNull(properties, "properties must not be null");
    NodeMetadata metadata = metadataRegistry.metadata(targetType);
    Object[] arguments = new Object[metadata.properties().size()];

    for (PropertyMetadata property : metadata.properties()) {
      Object value = properties.get(property.graphName());
      if (value == null && property.javaType().isPrimitive()) {
        throw new MetadataMappingException(
            "missing non-null property '" + property.graphName() + "' for " + targetType.getName());
      }
      arguments[property.constructorIndex()] =
          coerce(value, property.javaType(), property.graphName());
    }

    try {
      return targetType.cast(constructor(targetType, metadata).newInstance(arguments));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException failure) {
      throw new MetadataMappingException("failed to construct " + targetType.getName(), failure);
    }
  }

  private Constructor<?> constructor(Class<?> targetType, NodeMetadata metadata) {
    return constructorCache.computeIfAbsent(
        targetType,
        ignored -> {
          Class<?>[] parameterTypes =
              metadata.properties().stream()
                  .map(PropertyMetadata::javaType)
                  .toArray(Class<?>[]::new);
          try {
            Constructor<?> constructor = targetType.getDeclaredConstructor(parameterTypes);
            if (!constructor.trySetAccessible()) {
              throw new MetadataException(
                  "record constructor is not accessible: " + targetType.getName());
            }
            return constructor;
          } catch (NoSuchMethodException failure) {
            throw new MetadataException(
                "canonical record constructor not found: " + targetType.getName(), failure);
          }
        });
  }

  private static Object coerce(Object value, Class<?> targetType, String propertyName) {
    if (value == null || boxed(targetType).isInstance(value)) {
      return value;
    }
    Class<?> boxedType = boxed(targetType);
    if (value instanceof Number number && Number.class.isAssignableFrom(boxedType)) {
      return coerceNumber(number, boxedType, propertyName);
    }
    if (boxedType == UUID.class && value instanceof String text) {
      return UUID.fromString(text);
    }
    if (boxedType.isEnum() && value instanceof String text) {
      return enumValue(boxedType, text, propertyName);
    }
    throw new MetadataMappingException(
        "cannot map property '"
            + propertyName
            + "' from "
            + value.getClass().getName()
            + " to "
            + targetType.getName());
  }

  private static Object coerceNumber(Number number, Class<?> targetType, String propertyName) {
    if (targetType == Double.class) {
      return number.doubleValue();
    }
    if (targetType == Float.class) {
      return number.floatValue();
    }

    long value = exactLong(number, propertyName);
    if (targetType == Long.class) {
      return value;
    }
    if (targetType == Integer.class && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
      return (int) value;
    }
    if (targetType == Short.class && value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
      return (short) value;
    }
    if (targetType == Byte.class && value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
      return (byte) value;
    }
    throw new MetadataMappingException(
        "numeric value for property '" + propertyName + "' is outside " + targetType.getName());
  }

  private static long exactLong(Number number, String propertyName) {
    try {
      if (number instanceof BigInteger integer) {
        return integer.longValueExact();
      }
      if (number instanceof BigDecimal decimal) {
        return decimal.longValueExact();
      }
      if (number instanceof Double || number instanceof Float) {
        return BigDecimal.valueOf(number.doubleValue()).longValueExact();
      }
      return number.longValue();
    } catch (ArithmeticException failure) {
      throw new MetadataMappingException(
          "numeric value for property '" + propertyName + "' is not an exact integer", failure);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static Object enumValue(Class<?> targetType, String value, String propertyName) {
    try {
      return Enum.valueOf((Class<? extends Enum>) targetType, value);
    } catch (IllegalArgumentException failure) {
      throw new MetadataMappingException(
          "unknown enum value '" + value + "' for property '" + propertyName + "'", failure);
    }
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
