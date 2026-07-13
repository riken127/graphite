package io.github.riken127.graphite.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shared reflection rules for metadata discovery and object construction. */
final class ReflectionMappingSupport {

  private ReflectionMappingSupport() {}

  static Constructor<?> constructor(Class<?> javaType) {
    if (javaType.isRecord()) {
      Class<?>[] parameterTypes =
          Arrays.stream(javaType.getRecordComponents())
              .map(RecordComponent::getType)
              .toArray(Class<?>[]::new);
      try {
        return javaType.getDeclaredConstructor(parameterTypes);
      } catch (NoSuchMethodException failure) {
        throw new MetadataException(
            "canonical record constructor not found: " + javaType.getName(), failure);
      }
    }

    List<Constructor<?>> constructors =
        Arrays.stream(javaType.getDeclaredConstructors())
            .filter(constructor -> !constructor.isSynthetic())
            .toList();
    List<Constructor<?>> selected =
        constructors.stream()
            .filter(constructor -> constructor.isAnnotationPresent(GraphConstructor.class))
            .toList();
    if (selected.size() > 1) {
      throw new MetadataException(
          "multiple @GraphConstructor declarations on " + javaType.getName());
    }
    Constructor<?> constructor;
    if (selected.size() == 1) {
      constructor = selected.getFirst();
    } else if (constructors.size() == 1) {
      constructor = constructors.getFirst();
    } else {
      throw new MetadataException(
          "mapped class must declare one constructor or select one with @GraphConstructor: "
              + javaType.getName());
    }
    if (constructor.getParameterCount() == 0) {
      throw new MetadataException(
          "mapped class constructor must declare at least one property: " + javaType.getName());
    }
    return constructor;
  }

  static List<ReflectedProperty> properties(Class<?> javaType, Constructor<?> constructor) {
    if (javaType.isRecord()) {
      return recordProperties(javaType);
    }
    List<Field> fields =
        Arrays.stream(javaType.getDeclaredFields())
            .filter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
            .toList();
    Parameter[] parameters = constructor.getParameters();
    if (fields.size() < parameters.length) {
      throw new MetadataException(
          "mapped constructor has more parameters than instance fields: " + javaType.getName());
    }
    java.util.ArrayList<ReflectedProperty> properties =
        new java.util.ArrayList<>(parameters.length);
    Set<Field> matchedFields = new HashSet<>();
    for (int index = 0; index < parameters.length; index++) {
      Parameter parameter = parameters[index];
      Field field = field(javaType, parameter, fields, matchedFields);
      matchedFields.add(field);
      GraphProperty graphProperty =
          firstAnnotation(GraphProperty.class, parameter, field, accessor(javaType, field));
      boolean id =
          firstAnnotation(GraphId.class, parameter, field, accessor(javaType, field)) != null;
      properties.add(
          new ReflectedProperty(
              field.getName(),
              graphProperty == null ? field.getName() : graphProperty.value(),
              parameter.getType(),
              parameter.getParameterizedType(),
              id,
              index));
    }
    return List.copyOf(properties);
  }

  private static List<ReflectedProperty> recordProperties(Class<?> javaType) {
    RecordComponent[] components = javaType.getRecordComponents();
    java.util.ArrayList<ReflectedProperty> properties =
        new java.util.ArrayList<>(components.length);
    for (int index = 0; index < components.length; index++) {
      RecordComponent component = components[index];
      GraphProperty graphProperty = component.getAnnotation(GraphProperty.class);
      properties.add(
          new ReflectedProperty(
              component.getName(),
              graphProperty == null ? component.getName() : graphProperty.value(),
              component.getType(),
              component.getGenericType(),
              component.isAnnotationPresent(GraphId.class),
              index));
    }
    return List.copyOf(properties);
  }

  private static Field field(
      Class<?> javaType, Parameter parameter, List<Field> fields, Set<Field> matchedFields) {
    List<Field> compatibleFields =
        fields.stream()
            .filter(field -> !matchedFields.contains(field))
            .filter(field -> boxed(parameter.getType()).equals(boxed(field.getType())))
            .toList();
    GraphProperty graphProperty = parameter.getAnnotation(GraphProperty.class);
    if (graphProperty != null) {
      List<Field> graphNameMatches =
          compatibleFields.stream()
              .filter(
                  field -> {
                    GraphProperty fieldProperty =
                        firstAnnotation(GraphProperty.class, field, accessor(javaType, field));
                    return fieldProperty != null
                        && fieldProperty.value().equals(graphProperty.value());
                  })
              .toList();
      if (graphNameMatches.size() == 1) {
        return graphNameMatches.getFirst();
      }
    }

    if (parameter.isNamePresent()) {
      for (Field field : compatibleFields) {
        if (field.getName().equals(parameter.getName())) {
          return field;
        }
      }
    }

    if (parameter.isAnnotationPresent(GraphId.class)) {
      List<Field> idMatches =
          compatibleFields.stream()
              .filter(
                  field -> firstAnnotation(GraphId.class, field, accessor(javaType, field)) != null)
              .toList();
      if (idMatches.size() == 1) {
        return idMatches.getFirst();
      }
    }

    if (compatibleFields.size() == 1) {
      return compatibleFields.getFirst();
    }

    throw new MetadataException(
        "constructor parameter names are not present and parameter '"
            + parameter.getName()
            + "' on "
            + javaType.getName()
            + " cannot be matched unambiguously; compile the model with -parameters or annotate "
            + "the parameter and matching field/accessor with @GraphId or @GraphProperty");
  }

  private static Method accessor(Class<?> javaType, Field field) {
    String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
    for (String name : List.of(field.getName(), "get" + suffix, "is" + suffix)) {
      try {
        Method method = javaType.getDeclaredMethod(name);
        if (method.getParameterCount() == 0) {
          return method;
        }
      } catch (NoSuchMethodException ignored) {
        // Try the next conventional accessor name.
      }
    }
    return null;
  }

  @SafeVarargs
  private static <A extends Annotation> A firstAnnotation(
      Class<A> annotationType, java.lang.reflect.AnnotatedElement... elements) {
    for (java.lang.reflect.AnnotatedElement element : elements) {
      if (element != null) {
        A annotation = element.getAnnotation(annotationType);
        if (annotation != null) {
          return annotation;
        }
      }
    }
    return null;
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

  record ReflectedProperty(
      String javaName,
      String graphName,
      Class<?> javaType,
      java.lang.reflect.Type genericType,
      boolean id,
      int constructorIndex) {}
}
