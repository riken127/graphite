package io.github.riken127.graphite.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Constructs records and constructor-backed immutable objects from graph property maps. */
public final class GraphObjectMapper {

  private final NodeMetadataRegistry metadataRegistry;
  private final GraphValueConverters converters;
  private final GraphObjectFactories factories;
  private final ConcurrentMap<Class<?>, Constructor<?>> constructorCache =
      new ConcurrentHashMap<>();

  public GraphObjectMapper(NodeMetadataRegistry metadataRegistry) {
    this(metadataRegistry, GraphValueConverters.empty(), GraphObjectFactories.empty());
  }

  /** Creates a mapper with application-specific value converters. */
  public GraphObjectMapper(NodeMetadataRegistry metadataRegistry, GraphValueConverters converters) {
    this(metadataRegistry, converters, GraphObjectFactories.empty());
  }

  /** Creates a mapper with converters and factories for custom construction strategies. */
  public GraphObjectMapper(
      NodeMetadataRegistry metadataRegistry,
      GraphValueConverters converters,
      GraphObjectFactories factories) {
    this.metadataRegistry =
        Objects.requireNonNull(metadataRegistry, "metadataRegistry must not be null");
    this.converters = Objects.requireNonNull(converters, "converters must not be null");
    this.factories = Objects.requireNonNull(factories, "factories must not be null");
  }

  /** Maps graph properties to an instance of the requested immutable type. */
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
          convert(value, property.genericType(), property.javaType(), property.graphName());
    }

    return construct(targetType, metadata, arguments);
  }

  private <T> T construct(Class<T> targetType, NodeMetadata metadata, Object[] arguments) {
    Optional<GraphObjectFactory<T>> factory = factories.factory(targetType);
    try {
      if (factory.isPresent()) {
        return Objects.requireNonNull(
            factory.get().create(arguments.clone()),
            "custom graph object factory returned null for " + targetType.getName());
      }
      return targetType.cast(constructor(targetType, metadata).newInstance(arguments));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException failure) {
      throw new MetadataMappingException("failed to construct " + targetType.getName(), failure);
    } catch (MetadataException failure) {
      throw failure;
    } catch (RuntimeException failure) {
      throw new MetadataMappingException(
          "custom factory failed for " + targetType.getName(), failure);
    }
  }

  private Constructor<?> constructor(Class<?> targetType, NodeMetadata metadata) {
    return constructorCache.computeIfAbsent(
        targetType,
        ignored -> {
          Constructor<?> constructor = ReflectionMappingSupport.constructor(targetType);
          Class<?>[] metadataTypes =
              metadata.properties().stream()
                  .map(PropertyMetadata::javaType)
                  .toArray(Class<?>[]::new);
          if (!java.util.Arrays.equals(constructor.getParameterTypes(), metadataTypes)) {
            throw new MetadataException(
                "mapped constructor does not match metadata for " + targetType.getName());
          }
          if (!constructor.trySetAccessible()) {
            throw new MetadataException(
                "mapped constructor is not accessible: " + targetType.getName());
          }
          return constructor;
        });
  }

  private Object convert(Object value, Type genericType, Class<?> targetType, String propertyName) {
    if (targetType == Optional.class) {
      Type elementType = typeArgument(genericType, 0, propertyName);
      if (value == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(
          convert(value, elementType, rawType(elementType, propertyName), propertyName));
    }
    if (value == null) {
      return null;
    }
    Optional<Object> custom = converters.convert(value, genericType);
    if (custom.isPresent()) {
      return custom.get();
    }
    if (Collection.class.isAssignableFrom(targetType)) {
      return convertCollection(value, genericType, targetType, propertyName);
    }
    if (Map.class.isAssignableFrom(targetType)) {
      return convertMap(value, genericType, propertyName);
    }
    if (boxed(targetType).isInstance(value)) {
      return value;
    }
    if (value instanceof Map<?, ?> nested) {
      return map(targetType, stringKeyMap(nested, propertyName));
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
    if (boxedType == Character.class && value instanceof String text && text.length() == 1) {
      return text.charAt(0);
    }
    throw new MetadataMappingException(
        "cannot map property '"
            + propertyName
            + "' from "
            + value.getClass().getName()
            + " to "
            + targetType.getName());
  }

  private Object convertCollection(
      Object value, Type genericType, Class<?> targetType, String propertyName) {
    if (!(value instanceof Iterable<?> iterable)) {
      throw new MetadataMappingException(
          "property '" + propertyName + "' must be iterable for " + targetType.getName());
    }
    Type elementType = typeArgument(genericType, 0, propertyName);
    Class<?> elementClass = rawType(elementType, propertyName);
    Collection<Object> converted =
        Set.class.isAssignableFrom(targetType) ? new LinkedHashSet<>() : new ArrayList<>();
    for (Object element : iterable) {
      converted.add(convert(element, elementType, elementClass, propertyName));
    }
    return Set.class.isAssignableFrom(targetType)
        ? Collections.unmodifiableSet((Set<?>) converted)
        : List.copyOf(converted);
  }

  private Object convertMap(Object value, Type genericType, String propertyName) {
    if (!(value instanceof Map<?, ?> source)) {
      throw new MetadataMappingException("property '" + propertyName + "' must be a map");
    }
    Type keyType = typeArgument(genericType, 0, propertyName);
    if (rawType(keyType, propertyName) != String.class) {
      throw new MetadataMappingException(
          "property '" + propertyName + "' requires String map keys");
    }
    Type valueType = typeArgument(genericType, 1, propertyName);
    Class<?> valueClass = rawType(valueType, propertyName);
    Map<String, Object> converted = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : source.entrySet()) {
      if (!(entry.getKey() instanceof String key)) {
        throw new MetadataMappingException(
            "property '" + propertyName + "' contains a non-string map key");
      }
      converted.put(key, convert(entry.getValue(), valueType, valueClass, propertyName));
    }
    return Collections.unmodifiableMap(converted);
  }

  private static Type typeArgument(Type type, int index, String propertyName) {
    if (type instanceof ParameterizedType parameterized
        && parameterized.getActualTypeArguments().length > index) {
      return parameterized.getActualTypeArguments()[index];
    }
    throw new MetadataMappingException(
        "property '" + propertyName + "' requires concrete generic type information");
  }

  private static Class<?> rawType(Type type, String propertyName) {
    if (type instanceof Class<?> javaType) {
      return javaType;
    }
    if (type instanceof ParameterizedType parameterized
        && parameterized.getRawType() instanceof Class<?> javaType) {
      return javaType;
    }
    throw new MetadataMappingException(
        "unsupported generic type for property '" + propertyName + "': " + type);
  }

  private static Map<String, Object> stringKeyMap(Map<?, ?> source, String propertyName) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : source.entrySet()) {
      if (!(entry.getKey() instanceof String key)) {
        throw new MetadataMappingException(
            "nested property '" + propertyName + "' contains a non-string key");
      }
      result.put(key, entry.getValue());
    }
    return result;
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
